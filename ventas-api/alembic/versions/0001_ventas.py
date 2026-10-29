from alembic import op
import sqlalchemy as sa

revision = "0001"
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    op.create_table("compradores",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("usuario", sa.String(100), nullable=False),
        sa.Column("documento", sa.String(30), nullable=False),
        sa.Column("nombre", sa.String(120), nullable=False),
        sa.Column("correo", sa.String(200), nullable=False),
        sa.UniqueConstraint("usuario", "documento",
                            name="uq_comprador_usuario_documento"))

    op.create_table("ventas",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("operacion_id", sa.String(36), nullable=False, unique=True),
        sa.Column("usuario", sa.String(100), nullable=False),
        sa.Column("comprador_id", sa.Integer,
                  sa.ForeignKey("compradores.id"), nullable=False),
        sa.Column("vehiculo_id", sa.Integer, nullable=False),
        sa.Column("idempotency_key", sa.String(100), nullable=False),
        sa.Column("request_hash", sa.String(64), nullable=False),
        sa.Column("estado", sa.String(20), nullable=False),
        sa.Column("reserva_id", sa.String(36)),
        sa.Column("precio_acordado", sa.Numeric(12, 2)),
        sa.Column("moneda", sa.String(3)),
        sa.Column("error_status", sa.Integer),
        sa.Column("error_detalle", sa.String(250)),
        sa.Column("notificacion", sa.String(20), nullable=False),
        sa.Column("fecha", sa.DateTime, nullable=False),
        sa.UniqueConstraint("usuario", "idempotency_key",
                            name="uq_venta_usuario_clave"))


def downgrade():
    raise RuntimeError("Reversión con borrado de tablas deshabilitada")
