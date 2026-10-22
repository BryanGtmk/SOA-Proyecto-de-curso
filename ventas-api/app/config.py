import os
from pathlib import Path
from dotenv import load_dotenv
load_dotenv(dotenv_path=Path(__file__).resolve().parent.parent / ".env")


def requerido(nombre):
    valor = os.environ.get(nombre,"")
    if not valor:
        raise RuntimeError(f"FALTA CONFIGURAR {nombre}")
    return valor
JAVA_URL = requerido("JAVA_URL").rstrip("/")
SERVICE_KEY = requerido("VENTAS_SERVICE_KEY")
NOTIF_URL = requerido("NOTIFICATIONES_URL").rstrip("/")
NOTIF_KEY = requerido("NOTIFICATIONES_KEY")

if min(len(SERVICE_KEY), len(NOTIF_KEY)) < 32:
    raise RuntimeError("las claves nimino 32 caracteres")


if SERVICE_KEY.startswith("ventas-java-python-aula-2026-key-9F2uN4bS6x8Q1m") or NOTIF_KEY.startswith("avisos-java-python-aula-2026-key-7G5nP3rQ8c6L2t"):
    raise RuntimeError("debe contner la clave entre apis")