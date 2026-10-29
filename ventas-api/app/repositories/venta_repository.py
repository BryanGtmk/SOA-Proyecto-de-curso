from sqlalchemy import select, or_

from app.models import Venta


class VentaRepository:
    def __init__(self, db):
        self.db = db

    def por_clave(self, usuario, clave):
        return self.db.scalar(select(Venta).where(
            Venta.usuario == usuario, Venta.idempotency_key == clave))

    def bloquear(self, id):
        return self.db.scalar(select(Venta).where(Venta.id == id)
                              .with_for_update())

    def pendientes(self):
        return self.db.scalars(select(Venta.id).where(or_(
            Venta.estado.in_(["PENDIENTE", "EN_REVISION"]),
            (Venta.estado == "COMPLETADA") &
            (Venta.notificacion != "ENVIADA")))
            .order_by(Venta.id).limit(100)).all()
