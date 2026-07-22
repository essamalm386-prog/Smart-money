"""AgentReport model — output from a single CrewAI agent for an asset."""
from __future__ import annotations

from typing import Optional

from sqlalchemy import JSON, Float, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database.base import Base, TimestampMixin


class AgentReport(Base, TimestampMixin):
    __tablename__ = "agent_reports"

    id: Mapped[int] = mapped_column(primary_key=True)
    asset_id: Mapped[int] = mapped_column(ForeignKey("assets.id", ondelete="CASCADE"), index=True)
    thesis_id: Mapped[Optional[int]] = mapped_column(
        ForeignKey("investment_theses.id", ondelete="CASCADE"), index=True
    )
    ticker: Mapped[str] = mapped_column(String(20), index=True)
    agent_role: Mapped[str] = mapped_column(String(60), index=True)
    score: Mapped[Optional[float]] = mapped_column(Float)
    summary: Mapped[Optional[str]] = mapped_column(Text)
    payload: Mapped[dict] = mapped_column(JSON, default=dict)

    asset: Mapped["Asset"] = relationship(back_populates="reports")
    thesis: Mapped[Optional["InvestmentThesis"]] = relationship(back_populates="reports")

    def __repr__(self) -> str:  # pragma: no cover
        return f"<AgentReport {self.agent_role} {self.ticker} score={self.score}>"
