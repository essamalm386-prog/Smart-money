"""Market scan schemas."""
from __future__ import annotations

from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field, field_validator

from app.schemas.thesis import ThesisOut


class ScanCreate(BaseModel):
    tickers: List[str] = Field(default_factory=list, description="Tickers to scan; empty = default universe")
    label: Optional[str] = None
    sector: Optional[str] = Field(default=None, description="Sector key to expand into tickers")
    domain: Optional[str] = Field(default=None, description="Theme/domain key to expand into tickers")

    @field_validator("tickers")
    @classmethod
    def _normalise(cls, value: List[str]) -> List[str]:
        seen, out = set(), []
        for t in value:
            t = (t or "").strip().upper()
            if t and t not in seen:
                seen.add(t)
                out.append(t)
        return out


class ScanOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    label: Optional[str] = None
    status: str
    requested_tickers: List[str] = []
    num_assets: int = 0
    avg_score: Optional[float] = None
    error: Optional[str] = None
    started_at: Optional[datetime] = None
    finished_at: Optional[datetime] = None


class ScanDetailOut(BaseModel):
    scan: ScanOut
    theses: List[ThesisOut] = []
