"""Asset model — a tradable instrument tracked by the platform."""
from __future__ import annotations

from typing import List, Optional

from sqlalchemy import Float, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin


class Asset(Base, TimestampMixin):
    __tablename__ = "assets"

    id: Mapped[int] = mapped_column(primary_key=True)
    ticker: Mapped[str] = mapped_column(String(20), unique=True, index=True, nullable=False)
    name: Mapped[Optional[str]] = mapped_column(String(255))
    sector: Mapped[Optional[str]] = mapped_column(String(120), index=True)
    industry: Mapped[Optional[str]] = mapped_column(String(120))
    country: Mapped[Optional[str]] = mapped_column(String(80))
    currency: Mapped[Optional[str]] = mapped_column(String(10))
    market_cap: Mapped[Optional[float]] = mapped_column(Float)
    last_price: Mapped[Optional[float]] = mapped_column(Float)
    # Denormalised latest conviction score for fast dashboard reads.
    conviction_score: Mapped[Optional[float]] = mapped_column(Float, index=True)
    summary: Mapped[Optional[str]] = mapped_column(Text)
    # Data provenance: "real" | "simulated", plus human provider label.
    data_source: Mapped[Optional[str]] = mapped_column(String(20), default="simulated")
    data_provider: Mapped[Optional[str]] = mapped_column(String(40))

    theses: Mapped[List["InvestmentThesis"]] = relationship(
        back_populates="asset", cascade="all, delete-orphan"
    )
    reports: Mapped[List["AgentReport"]] = relationship(
        back_populates="asset", cascade="all, delete-orphan"
    )
    scores: Mapped[List["HistoricalScore"]] = relationship(
        back_populates="asset", cascade="all, delete-orphan"
    )

    def __repr__(self) -> str:  # pragma: no cover
        return f"<Asset {self.ticker} score={self.conviction_score}>"
