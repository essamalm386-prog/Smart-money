"""System log endpoints."""
from __future__ import annotations

from typing import List

from fastapi import APIRouter, Depends, Query
from sqlalchemy import desc, select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import SystemLog
from app.schemas import SystemLogOut

router = APIRouter()


@router.get("", response_model=List[SystemLogOut])
def list_logs(
    db: Session = Depends(get_db),
    level: str | None = Query(default=None),
    limit: int = Query(default=100, le=1000),
):
    stmt = select(SystemLog)
    if level:
        stmt = stmt.where(SystemLog.level == level.upper())
    stmt = stmt.order_by(desc(SystemLog.created_at)).limit(limit)
    return list(db.scalars(stmt))
