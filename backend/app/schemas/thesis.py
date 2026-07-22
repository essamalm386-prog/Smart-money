"""Investment thesis schemas."""
from __future__ import annotations

from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field

from app.schemas.report import AgentReportOut


class SubScores(BaseModel):
    financial_score: float = Field(ge=0, le=100)
    business_score: float = Field(ge=0, le=100)
    future_score: float = Field(ge=0, le=100)
    contrarian_score: float = Field(ge=0, le=100)
    geopolitical_score: float = Field(ge=0, le=100)


class ThesisOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ticker: str
    name: Optional[str] = None
    scan_id: Optional[int] = None
    financial_score: float
    business_score: float
    future_score: float
    contrarian_score: float
    geopolitical_score: float = 50.0
    conviction_score: float
    recommendation: str
    geo_risk: Optional[str] = None
    outlook: Optional[str] = None
    volatility: Optional[str] = None
    summary: Optional[str] = None
    engine: str = "heuristic"
    data_source: Optional[str] = None       # "real" | "simulated"
    data_provider: Optional[str] = None      # e.g. "Yahoo Finance"
    created_at: Optional[datetime] = None


class ThesisDetailOut(ThesisOut):
    thesis: Optional[str] = None
    risks: Optional[str] = None
    catalysts: Optional[str] = None
    agent_reports: List[AgentReportOut] = []
