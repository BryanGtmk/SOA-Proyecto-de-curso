from datetime import datetime, timezone
from decimal import Decimal

from sqlalchemy import String, Numeric, ForeignKey, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db import Base


class Comprador(Base):
    __tablename__ = "compradores"
    __table_args__ = (
        UniqueConstraint("usuario", "documento",
                         name="uq_comprador_usuario_documento"),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    usuario: Mapped[str] = mapped_column(String(100))
    documento: Mapped[str] = mapped_column(String(30))
    nombre: Mapped[str] = mapped_column(String(120))
    correo: Mapped[str] = mapped_column(String(200))

    ventas: Mapped[list["Venta"]] = relationship(
        back_populates="comprador")


class Venta(Base):
    __tablename__ = "ventas"
    __table_args__ = (
        UniqueConstraint("usuario", "idempotency_key",
                         name="uq_venta_usuario_clave"),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    operacion_id: Mapped[str] = mapped_column(String(36), unique=True)
    usuario: Mapped[str] = mapped_column(String(100))
    comprador_id: Mapped[int] = mapped_column(
        ForeignKey("compradores.id"))
    vehiculo_id: Mapped[int]
    idempotency_key: Mapped[str] = mapped_column(String(100))
    request_hash: Mapped[str] = mapped_column(String(64))
    estado: Mapped[str] = mapped_column(String(20), default="PENDIENTE")
    reserva_id: Mapped[str | None] = mapped_column(String(36))
    precio_acordado: Mapped[Decimal | None] = mapped_column(Numeric(12, 2))
    moneda: Mapped[str | None] = mapped_column(String(3))
    error_status: Mapped[int | None]
    error_detalle: Mapped[str | None] = mapped_column(String(250))
    notificacion: Mapped[str] = mapped_column(String(20), default="PENDIENTE")
    fecha: Mapped[datetime] = mapped_column(
        default=lambda: datetime.now(timezone.utc).replace(tzinfo=None))

    comprador: Mapped[Comprador] = relationship(back_populates="ventas")
