import sys
import time
import logging

from app.db import SessionLocal
from app.repositories.venta_repository import VentaRepository
from app.services.venta_service import procesar, notificar


def ciclo():
    with SessionLocal() as db:
        ids = VentaRepository(db).pendientes()
    for id in ids:
        try:
            procesar(id)
            notificar(id)
        except Exception:
            logging.exception("No se pudo conciliar venta %s", id)


if __name__ == "__main__":
    while True:
        try:
            ciclo()
        except Exception:
            logging.exception("Fallo de ciclo de recuperación")
        if "--once" in sys.argv:
            break
        time.sleep(10)
