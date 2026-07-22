"""System, config, health and log schemas."""
from __future__ import annotations

from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict


class HealthOut(BaseModel):
    status: str = "ok"
    time: datetime
    version: str


class ScorePoint(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    conviction_score: float
    created_at: datetime


class SystemLogOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    level: str
    source: Optional[str] = None
    message: str
    created_at: Optional[datetime] = None


class ConfigOut(BaseModel):
    min_market_cap: float
    excluded_sectors: List[str]
    max_pe: float
    scan_universe: List[str]
    use_llm: bool
    anthropic_configured: bool
    pappers_configured: bool = False
    llm_model: str


class ConfigUpdate(BaseModel):
    min_market_cap: Optional[float] = None
    excluded_sectors: Optional[List[str]] = None
    max_pe: Optional[float] = None
    scan_universe: Optional[List[str]] = None
    use_llm: Optional[bool] = None
