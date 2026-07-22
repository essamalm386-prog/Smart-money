"""Watchlist schemas."""
from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field, field_validator


class WatchlistCreate(BaseModel):
    ticker: str = Field(min_length=1, max_length=20)
    note: Optional[str] = None

    @field_validator("ticker")
    @classmethod
    def _upper(cls, v: str) -> str:
        return v.strip().upper()


class WatchlistOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ticker: str
    name: Optional[str] = None
    conviction_score: Optional[float] = None
    note: Optional[str] = None
    created_at: Optional[datetime] = None
