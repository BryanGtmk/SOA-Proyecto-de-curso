import httpx

from app.config import JAVA_URL, SERVICE_KEY


class FalloRemoto(Exception):
    def __init__(self, status, detalle):
        self.status, self.detalle = status, detalle


class VehiculosClient:
    def llamar(self, metodo, ruta, **kwargs):
        try:
            r = httpx.request(metodo, JAVA_URL + ruta,
                headers={"X-Service-Key": SERVICE_KEY},
                timeout=httpx.Timeout(5, connect=2), **kwargs)
        except httpx.RequestError:
            raise FalloRemoto(503, "Resultado remoto desconocido")
        if r.status_code >= 400:
            raise FalloRemoto(r.status_code, "Operación Java rechazada")
        try:
            return r.json() if r.content else None
        except ValueError:
            raise FalloRemoto(502, "Respuesta Java ilegible")

    def por_operacion(self, op):
        return self.llamar("GET", "/api/reservas",
                           params={"operacionId": op})

    def reservar(self, vehiculo, op):
        return self.llamar("POST", f"/api/vehiculos/{vehiculo}/reservas",
                           json={"operacionId": op})

    def confirmar(self, reserva):
        return self.llamar("PUT", f"/api/reservas/{reserva}/confirmacion")
