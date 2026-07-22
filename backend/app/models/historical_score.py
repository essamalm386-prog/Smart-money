"""HistoricalScore model — time series of conviction scores per asset."""
from __future__ import annotations

from typing import Optional

from sqlalchemy import Float, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin


class HistoricalScore(Base, TimestampMixin):
    __tablename__ = "historical_scores"

    id: Mapped[int] = mapped_column(primary_key=True)
    asset_id: Mapped[int] = mapped_column(ForeignKey("assets.id", ondelete="CASCADE"), index=True)
    ticker: Mapped[str] = mapped_column(String(20), index=True)
    conviction_score: Mapped[float] = mapped_column(Float)
    financial_score: Mapped[Optional[float]] = mapped_column(Float)
    business_score: Mapped[Optional[float]] = mapped_column(Float)
    future_score: Mapped[Optional[float]] = mapped_column(Float)
    contrarian_score: Mapped[Optional[float]] = mapped_column(Float)

    asset: Mapped["Asset"] = relationship(back_populates="scores")

    def __repr__(self) -> str:  # pragma: no cover
        return f"<HistoricalScore {self.ticker} {self.conviction_score}>"
