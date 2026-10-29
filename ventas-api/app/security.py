import httpx
from fastapi import Header

from app.config import JAVA_URL
from app.errors import ApiError


def identidad(authorization: str | None = Header(default=None)):
    if not authorization or not authorization.startswith("Bearer "):
        raise ApiError(401, "NO_AUTENTICADO", "Enviar access token")
    try:
        r = httpx.get(JAVA_URL + "/api/identidad",
                      headers={"Authorization": authorization}, timeout=5)
    except httpx.RequestError:
        raise ApiError(503, "IDENTIDAD_NO_DISPONIBLE", "Java no responde")
    if r.status_code in (401, 403):
        raise ApiError(r.status_code, "ACCESO_RECHAZADO", "Token no autorizado")
    if r.status_code != 200:
        raise ApiError(503, "IDENTIDAD_NO_DISPONIBLE", "No se validó la identidad")
    try:
        data = r.json()
        if not isinstance(data, dict):
            raise ValueError("No es un objeto")
        if not isinstance(data.get("usuario"), str):
            raise ValueError("Usuario ausente")
        if not isinstance(data.get("roles"), list):
            raise ValueError("Roles ausentes")
    except (ValueError, TypeError):
        raise ApiError(503, "IDENTIDAD_INVALIDA", "Respuesta de Java inválida")
    if len(data["usuario"]) > 100:
        raise ApiError(403, "IDENTIDAD_INVALIDA", "Usuario demasiado largo")
    return data


def vendedor(usuario):
    if not {"ROLE_OPERADOR", "ROLE_ADMIN"}.intersection(usuario["roles"]):
        raise ApiError(403, "SIN_PERMISO", "Se requiere OPERADOR o ADMIN")


def propietario(usuario, dueno):
    if usuario["usuario"] != dueno and "ROLE_ADMIN" not in usuario["roles"]:
        raise ApiError(403, "SIN_PERMISO", "Recurso de otro usuario")
