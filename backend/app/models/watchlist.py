"""Watchlist model — assets the user is tracking."""
from __future__ import annotations

from typing import Optional

from sqlalchemy import Float, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.database.base import Base, TimestampMixin


class Watchlist(Base, TimestampMixin):
    __tablename__ = "watchlists"

    id: Mapped[int] = mapped_column(primary_key=True)
    ticker: Mapped[str] = mapped_column(String(20), unique=True, index=True, nullable=False)
    name: Mapped[Optional[str]] = mapped_column(String(255))
    conviction_score: Mapped[Optional[float]] = mapped_column(Float)
    note: Mapped[Optional[str]] = mapped_column(Text)

    def __repr__(self) -> str:  # pragma: no cover
        return f"<Watchlist {self.ticker}>"
