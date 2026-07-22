"""Assets endpoints."""
from __future__ import annotations

from typing import List

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import desc, select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Asset, AgentReport, HistoricalScore, InvestmentThesis
from app.schemas import AssetDetailOut, AssetOut
from app.schemas.report import AgentReportOut
from app.schemas.system import ScorePoint
from app.schemas.thesis import ThesisOut

router = APIRouter()


@router.get("", response_model=List[AssetOut])
def list_assets(
    db: Session = Depends(get_db),
    sector: str | None = Query(default=None),
    limit: int = Query(default=200, le=1000),
) -> List[Asset]:
    stmt = select(Asset)
    if sector:
        stmt = stmt.where(Asset.sector == sector)
    stmt = stmt.order_by(desc(Asset.conviction_score)).limit(limit)
    return list(db.scalars(stmt))


@router.get("/{ticker}", response_model=AssetDetailOut)
def get_asset(ticker: str, db: Session = Depends(get_db)) -> AssetDetailOut:
    ticker = ticker.upper()
    asset = db.scalar(select(Asset).where(Asset.ticker == ticker))
    if asset is None:
        raise HTTPException(status_code=404, detail=f"Asset {ticker} not found")

    latest = db.scalar(
        select(InvestmentThesis)
        .where(InvestmentThesis.asset_id == asset.id)
        .order_by(desc(InvestmentThesis.created_at))
        .limit(1)
    )
    reports = list(
        db.scalars(
            select(AgentReport)
            .where(AgentReport.thesis_id == (latest.id if latest else -1))
            .order_by(AgentReport.id)
        )
    )
    history = list(
        db.scalars(
            select(HistoricalScore)
            .where(HistoricalScore.asset_id == asset.id)
            .order_by(HistoricalScore.created_at)
        )
    )
    return AssetDetailOut(
        asset=AssetOut.model_validate(asset),
        latest_thesis=ThesisOut.model_validate(latest) if latest else None,
        reports=[AgentReportOut.model_validate(r) for r in reports],
        score_history=[ScorePoint.model_validate(h) for h in history],
    )
