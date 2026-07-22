"""Market scan endpoints."""
from __future__ import annotations

from typing import List

from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException, Query
from sqlalchemy import desc, select
from sqlalchemy.orm import Session

from app.config import settings
from app.database import get_db
from app.models import InvestmentThesis, MarketScan
from app.schemas import ScanCreate, ScanDetailOut, ScanOut
from app.schemas.thesis import ThesisOut
from app.services.universe import sector_label, tickers_for_domain, tickers_for_sector
from app.tasks import run_scan

router = APIRouter()


def _resolve_tickers(payload: ScanCreate) -> tuple[list[str], str]:
    """Resolve the ticker list + a default label from tickers/sector/domain."""
    if payload.tickers:
        return payload.tickers, payload.label or "Scan manuel"
    if payload.sector:
        tk = tickers_for_sector(payload.sector)
        if tk:
            return tk, payload.label or f"Secteur : {sector_label(payload.sector)}"
    if payload.domain:
        from app.services.universe import DOMAINS
        tk = tickers_for_domain(payload.domain)
        if tk:
            lbl = DOMAINS.get(payload.domain.lower().strip(), {}).get("label", payload.domain)
            return tk, payload.label or f"Thématique : {lbl}"
    return list(settings.default_universe), payload.label or "Scan général"


@router.get("", response_model=List[ScanOut])
def list_scans(db: Session = Depends(get_db), limit: int = Query(default=50, le=200)):
    stmt = select(MarketScan).order_by(desc(MarketScan.created_at)).limit(limit)
    return list(db.scalars(stmt))


@router.post("", response_model=ScanOut, status_code=201)
def create_scan(
    payload: ScanCreate,
    background: BackgroundTasks,
    db: Session = Depends(get_db),
) -> MarketScan:
    tickers, label = _resolve_tickers(payload)
    scan = MarketScan(
        label=label,
        status="pending",
        requested_tickers=tickers,
    )
    db.add(scan)
    db.commit()
    db.refresh(scan)
    # Run asynchronously so the request returns immediately.
    background.add_task(run_scan, scan.id, tickers, settings.enable_network)
    return scan


@router.get("/{scan_id}", response_model=ScanDetailOut)
def get_scan(scan_id: int, db: Session = Depends(get_db)) -> ScanDetailOut:
    scan = db.get(MarketScan, scan_id)
    if scan is None:
        raise HTTPException(status_code=404, detail="Scan not found")
    theses = list(
        db.scalars(
            select(InvestmentThesis)
            .where(InvestmentThesis.scan_id == scan_id)
            .order_by(desc(InvestmentThesis.conviction_score))
        )
    )
    return ScanDetailOut(
        scan=ScanOut.model_validate(scan),
        theses=[ThesisOut.model_validate(t) for t in theses],
    )
