"""Asset schemas."""
from __future__ import annotations

from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict

from app.schemas.report import AgentReportOut
from app.schemas.system import ScorePoint
from app.schemas.thesis import ThesisOut


class AssetOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ticker: str
    name: Optional[str] = None
    sector: Optional[str] = None
    industry: Optional[str] = None
    country: Optional[str] = None
    market_cap: Optional[float] = None
    currency: Optional[str] = None
    last_price: Optional[float] = None
    conviction_score: Optional[float] = None
    data_source: Optional[str] = None       # "real" | "simulated"
    data_provider: Optional[str] = None      # e.g. "Yahoo Finance"
    updated_at: Optional[datetime] = None


class AssetDetailOut(BaseModel):
    asset: AssetOut
    latest_thesis: Optional[ThesisOut] = None
    reports: List[AgentReportOut] = []
    score_history: List[ScorePoint] = []
