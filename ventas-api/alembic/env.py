from alembic import context

from app.db import Base, engine
from app import models

TABLAS_PYTHON = {"compradores", "ventas"}

target_metadata = Base.metadata


def incluir_nombre(nombre, tipo, nombres_padre):
    if tipo == "table":
        return nombre in TABLAS_PYTHON
    return True


def incluir_objeto(objeto, nombre, tipo, reflejado, comparar):
    if tipo == "table":
        return nombre in TABLAS_PYTHON
    return True


def run_migrations_offline():
    context.configure(
        url=str(engine.url),
        target_metadata=target_metadata,
        include_name=incluir_nombre,
        include_object=incluir_objeto,
        compare_type=True,
        literal_binds=True,
    )
    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online():
    with engine.connect() as connection:
        context.configure(
            connection=connection,
            target_metadata=target_metadata,
            include_name=incluir_nombre,
            include_object=incluir_objeto,
            compare_type=True,
        )
        with context.begin_transaction():
            context.run_migrations()


if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
