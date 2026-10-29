import hashlib
import json
from uuid import uuid4
from decimal import Decimal

import httpx
from sqlalchemy.exc import IntegrityError

from app.db import SessionLocal
from app.models import Venta, Comprador
from app.repositories.venta_repository import VentaRepository
from app.clients.vehiculos_client import VehiculosClient, FalloRemoto
from app.config import NOTIF_URL, NOTIF_KEY
from app.errors import ApiError
from app.security import propietario

remote = VehiculosClient()


def salida(v):
    return {"id": v.id, "operacionId": v.operacion_id,
            "vehiculoId": v.vehiculo_id, "compradorId": v.comprador_id,
            "precioAcordado": str(v.precio_acordado)
                if v.precio_acordado is not None else None,
            "moneda": v.moneda, "estado": v.estado,
            "notificacion": v.notificacion,
            "fechaUTC": v.fecha.isoformat() + "Z"}


def crear(datos, clave, usuario):
    cuerpo = json.dumps(datos.model_dump(), sort_keys=True,
                        separators=(",", ":"))
    huella = hashlib.sha256(cuerpo.encode()).hexdigest()
    nueva = False

    with SessionLocal() as db:
        repo = VentaRepository(db)
        v = repo.por_clave(usuario["usuario"], clave)
        if v is None:
            comprador = db.get(Comprador, datos.compradorId)
            if comprador is None:
                raise ApiError(404, "COMPRADOR_NO_EXISTE", "Comprador inexistente")
            propietario(usuario, comprador.usuario)
            v = Venta(operacion_id=str(uuid4()), usuario=usuario["usuario"],
                      vehiculo_id=datos.vehiculoId, comprador_id=datos.compradorId,
                      idempotency_key=clave, request_hash=huella)
            db.add(v)
            try:
                db.commit()
                nueva = True
            except IntegrityError:
                db.rollback()
                v = repo.por_clave(usuario["usuario"], clave)
                if v is None:
                    raise
        if v.request_hash != huella:
            raise ApiError(409, "CLAVE_REUTILIZADA",
                           "La misma clave requiere el mismo cuerpo")
        id = v.id

    procesar(id)
    notificar(id)

    with SessionLocal() as db:
        v = db.get(Venta, id)
        if v.estado == "FALLIDA":
            raise ApiError(v.error_status or 409, "VENTA_RECHAZADA",
                           v.error_detalle or "La venta fue rechazada")
        status = (201 if nueva else 200) if v.estado == "COMPLETADA" else 202
        return salida(v), status


def procesar(id):
    with SessionLocal.begin() as db:
        v = VentaRepository(db).bloquear(id)
        if v is None or v.estado in ("COMPLETADA", "FALLIDA"):
            return
        try:
            try:
                r = remote.por_operacion(v.operacion_id)
            except FalloRemoto as ex:
                if ex.status != 404:
                    raise
                try:
                    r = remote.reservar(v.vehiculo_id, v.operacion_id)
                except FalloRemoto as rechazo:
                    if rechazo.status in (404, 409, 422):
                        v.estado = "FALLIDA"
                        v.error_status = rechazo.status
                        v.error_detalle = "Vehículo inexistente o no disponible"
                        return
                    raise
            v.reserva_id = r["id"]
            v.precio_acordado = Decimal(str(r["precio"]))
            v.moneda = r["moneda"]
            if r["estado"] == "LIBERADA":
                v.estado = "FALLIDA"; v.error_status = 409
                v.error_detalle = "Reserva liberada"
                return
            if r["estado"] == "RESERVADA":
                r = remote.confirmar(r["id"])
            if r["estado"] != "CONFIRMADA":
                raise FalloRemoto(502, "Estado de reserva inesperado")
            v.estado = "COMPLETADA"
            v.error_status = None; v.error_detalle = None
        except (FalloRemoto, ValueError, KeyError) as ex:
            v.estado = "EN_REVISION"
            v.error_detalle = "Pendiente de consultar y conciliar con Java"


def notificar(id):
    with SessionLocal.begin() as db:
        v = VentaRepository(db).bloquear(id)
        if v is None or v.estado != "COMPLETADA" or v.notificacion == "ENVIADA":
            return
        try:
            r = httpx.post(NOTIF_URL + "/api/notificaciones", timeout=3,
                headers={"X-Notification-Key": NOTIF_KEY}, json={
                    "vehiculoId": v.vehiculo_id, "tipo": "VENTA",
                    "destinatario": v.comprador.correo,
                    "mensaje": f"Venta {v.id} operación {v.operacion_id} completada",
                })
            v.notificacion = "ENVIADA" if 200 <= r.status_code < 300 else "PENDIENTE"
        except httpx.RequestError:
            v.notificacion = "PENDIENTE"
