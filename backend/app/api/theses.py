"""Investment thesis endpoints — the ranked opportunity list."""
from __future__ import annotations

from typing import List

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import desc, select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import AgentReport, InvestmentThesis
from app.schemas import ThesisDetailOut, ThesisOut
from app.schemas.report import AgentReportOut

router = APIRouter()


@router.get("", response_model=List[ThesisOut])
def list_theses(
    db: Session = Depends(get_db),
    limit: int = Query(default=50, le=500),
    latest_per_ticker: bool = Query(default=True),
) -> List[ThesisOut]:
    """Return theses ranked by conviction (highest first).

    By default only the most recent thesis per ticker is returned so the
    dashboard shows current opportunities, not historical duplicates.
    """
    stmt = select(InvestmentThesis).order_by(desc(InvestmentThesis.created_at))
    rows = list(db.scalars(stmt))
    if latest_per_ticker:
        seen: set[str] = set()
        deduped: List[InvestmentThesis] = []
        for t in rows:
            if t.ticker not in seen:
                seen.add(t.ticker)
                deduped.append(t)
        rows = deduped
    rows.sort(key=lambda t: t.conviction_score or 0, reverse=True)
    return [ThesisOut.model_validate(t) for t in rows[:limit]]


@router.get("/{thesis_id}", response_model=ThesisDetailOut)
def get_thesis(thesis_id: int, db: Session = Depends(get_db)) -> ThesisDetailOut:
    thesis = db.get(InvestmentThesis, thesis_id)
    if thesis is None:
        raise HTTPException(status_code=404, detail="Thesis not found")
    reports = list(
        db.scalars(
            select(AgentReport)
            .where(AgentReport.thesis_id == thesis_id)
            .order_by(AgentReport.id)
        )
    )
    detail = ThesisDetailOut.model_validate(thesis)
    detail.agent_reports = [AgentReportOut.model_validate(r) for r in reports]
    return detail
