import os

from sqlalchemy import create_engine, URL
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from app.config import requerido

url = URL.create(
    "mysql+pymysql",
    username=requerido("DB_USER"),
    password=os.getenv("DB_PASSWORD", ""),
    host=requerido("DB_HOST"),
    port=int(os.getenv("DB_PORT", "3306")),
    database=requerido("DB_NAME"),
    query={"charset": "utf8mb4"},
)

engine = create_engine(url, pool_pre_ping=True,
                       isolation_level="READ COMMITTED")

SessionLocal = sessionmaker(engine, expire_on_commit=False)


class Base(DeclarativeBase):
    pass
