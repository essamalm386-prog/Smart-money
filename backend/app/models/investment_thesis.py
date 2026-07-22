"""InvestmentThesis model — the committee's verdict on one asset for one scan."""
from __future__ import annotations

from typing import List, Optional

from sqlalchemy import Float, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin


class InvestmentThesis(Base, TimestampMixin):
    __tablename__ = "investment_theses"

    id: Mapped[int] = mapped_column(primary_key=True)
    asset_id: Mapped[int] = mapped_column(ForeignKey("assets.id", ondelete="CASCADE"), index=True)
    scan_id: Mapped[Optional[int]] = mapped_column(
        ForeignKey("market_scans.id", ondelete="SET NULL"), index=True
    )
    ticker: Mapped[str] = mapped_column(String(20), index=True)
    name: Mapped[Optional[str]] = mapped_column(String(255))

    # Sub-scores (0-100) and final weighted conviction score.
    financial_score: Mapped[float] = mapped_column(Float, default=0.0)
    business_score: Mapped[float] = mapped_column(Float, default=0.0)
    future_score: Mapped[float] = mapped_column(Float, default=0.0)
    contrarian_score: Mapped[float] = mapped_column(Float, default=0.0)
    geopolitical_score: Mapped[float] = mapped_column(Float, default=50.0)
    conviction_score: Mapped[float] = mapped_column(Float, default=0.0, index=True)

    recommendation: Mapped[str] = mapped_column(String(30), default="HOLD")
    # Geostrategic forecast of future fluctuations.
    geo_risk: Mapped[Optional[str]] = mapped_column(String(20))          # Faible|Modéré|Élevé
    outlook: Mapped[Optional[str]] = mapped_column(String(20))           # Haussière|Neutre|Prudente
    volatility: Mapped[Optional[str]] = mapped_column(String(20))        # Faible|Modérée|Élevée
    summary: Mapped[Optional[str]] = mapped_column(Text)
    thesis: Mapped[Optional[str]] = mapped_column(Text)
    risks: Mapped[Optional[str]] = mapped_column(Text)
    catalysts: Mapped[Optional[str]] = mapped_column(Text)
    engine: Mapped[str] = mapped_column(String(20), default="heuristic")  # heuristic|llm
    data_source: Mapped[Optional[str]] = mapped_column(String(20), default="simulated")
    data_provider: Mapped[Optional[str]] = mapped_column(String(40))

    asset: Mapped["Asset"] = relationship(back_populates="theses")
    scan: Mapped[Optional["MarketScan"]] = relationship(back_populates="theses")
    reports: Mapped[List["AgentReport"]] = relationship(
        back_populates="thesis", cascade="all, delete-orphan"
    )

    def __repr__(self) -> str:  # pragma: no cover
        return f"<Thesis {self.ticker} conviction={self.conviction_score} rec={self.recommendation}>"
