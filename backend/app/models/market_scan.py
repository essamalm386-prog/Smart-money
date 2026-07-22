"""MarketScan model — one execution of the discovery pipeline."""
from __future__ import annotations

from datetime import datetime
from typing import List, Optional

from sqlalchemy import JSON, DateTime, Float, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin


class MarketScan(Base, TimestampMixin):
    __tablename__ = "market_scans"

    id: Mapped[int] = mapped_column(primary_key=True)
    label: Mapped[Optional[str]] = mapped_column(String(255))
    status: Mapped[str] = mapped_column(String(20), default="pending", index=True)
    requested_tickers: Mapped[list] = mapped_column(JSON, default=list)
    num_assets: Mapped[int] = mapped_column(Integer, default=0)
    avg_score: Mapped[Optional[float]] = mapped_column(Float)
    error: Mapped[Optional[str]] = mapped_column(String(1000))
    started_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True))
    finished_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True))

    theses: Mapped[List["InvestmentThesis"]] = relationship(back_populates="scan")

    def __repr__(self) -> str:  # pragma: no cover
        return f"<MarketScan #{self.id} status={self.status} n={self.num_assets}>"
