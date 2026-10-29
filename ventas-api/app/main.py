from fastapi import FastAPI

from app.errors import instalar
from app.routers.ventas import router

app = FastAPI(title="API de ventas de vehículos", version="1.0.0")

instalar(app)
app.include_router(router)


@app.get("/health")
def health():
    return {"status": "UP"}
