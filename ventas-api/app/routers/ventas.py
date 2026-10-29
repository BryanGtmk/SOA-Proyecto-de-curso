from typing import Annotated

from fastapi import APIRouter, Depends, Header, Response

from app.db import SessionLocal
from app.models import Venta, Comprador
from app.schemas import CrearVenta, CrearComprador
from app.security import identidad, vendedor, propietario
from app.errors import ApiError
from app.services import venta_service as service

router = APIRouter(prefix="/api")


@router.post("/compradores", status_code=201)
def crear_comprador(d: CrearComprador, u=Depends(identidad)):
    vendedor(u)
    with SessionLocal.begin() as db:
        c = Comprador(usuario=u["usuario"], **d.model_dump())
        db.add(c); db.flush()
        return {"id": c.id, "nombre": c.nombre, "correo": c.correo}


@router.post("/ventas")
def crear(d: CrearVenta, response: Response,
          idempotency_key: Annotated[str, Header(
              min_length=8, max_length=100, pattern=r"^[A-Za-z0-9_-]+$")],
          u=Depends(identidad)):
    vendedor(u)
    resultado, codigo = service.crear(d, idempotency_key, u)
    response.status_code = codigo
    response.headers["Location"] = f"/api/ventas/{resultado['id']}"
    return resultado


@router.get("/ventas/{id}")
def obtener(id: int, u=Depends(identidad)):
    with SessionLocal() as db:
        v = db.get(Venta, id)
        if v is None:
            raise ApiError(404, "VENTA_NO_EXISTE", "Venta inexistente")
        propietario(u, v.usuario)
        return service.salida(v)
