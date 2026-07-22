"""Agent report schemas."""
from __future__ import annotations

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict


class AgentReportOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    ticker: str
    agent_role: str
    score: Optional[float] = None
    summary: Optional[str] = None
    payload: dict = {}
    created_at: Optional[datetime] = None
