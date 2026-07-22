"""SystemLog model — persisted structured application events."""
from __future__ import annotations

from typing import Optional

from sqlalchemy import JSON, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin


class SystemLog(Base, TimestampMixin):
    __tablename__ = "system_logs"

    id: Mapped[int] = mapped_column(primary_key=True)
    level: Mapped[str] = mapped_column(String(20), default="INFO", index=True)
    source: Mapped[Optional[str]] = mapped_column(String(120))
    message: Mapped[str] = mapped_column(Text)
    context: Mapped[dict] = mapped_column(JSON, default=dict)

    def __repr__(self) -> str:  # pragma: no cover
        return f"<SystemLog {self.level} {self.source}>"
