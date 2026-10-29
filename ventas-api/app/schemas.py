from pydantic import BaseModel, ConfigDict, Field


class CrearComprador(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)

    documento: str = Field(min_length=3, max_length=30)
    nombre: str = Field(min_length=2, max_length=120)
    correo: str = Field(min_length=5, max_length=200,
                        pattern=r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


class CrearVenta(BaseModel):
    model_config = ConfigDict(extra="forbid")

    vehiculoId: int = Field(gt=0, strict=True)
    compradorId: int = Field(gt=0, strict=True)
