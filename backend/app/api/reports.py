"""Agent report feed endpoints."""
from __future__ import annotations

from typing import List

from fastapi import APIRouter, Depends, Query
from sqlalchemy import desc, select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import AgentReport
from app.schemas import AgentReportOut

router = APIRouter()


@router.get("", response_model=List[AgentReportOut])
def list_reports(
    db: Session = Depends(get_db),
    agent_role: str | None = Query(default=None),
    ticker: str | None = Query(default=None),
    limit: int = Query(default=50, le=500),
):
    stmt = select(AgentReport)
    if agent_role:
        stmt = stmt.where(AgentReport.agent_role == agent_role)
    if ticker:
        stmt = stmt.where(AgentReport.ticker == ticker.upper())
    stmt = stmt.order_by(desc(AgentReport.created_at)).limit(limit)
    return list(db.scalars(stmt))
