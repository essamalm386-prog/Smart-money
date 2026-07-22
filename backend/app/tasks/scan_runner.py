"""Scan runner — executes a market scan and persists results.

Designed to run either inline (tests) or in a FastAPI BackgroundTask. It owns
its own DB session so it is safe to run detached from the request lifecycle.
"""
from __future__ import annotations

from datetime import datetime, timezone
from typing import List, Optional

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.agents.pipeline import AnalysisResult, analyze_asset
from app.config import settings
from app.database.session import SessionLocal
from app.logging_config import get_logger
from app.models import (
    AgentReport,
    Asset,
    HistoricalScore,
    InvestmentThesis,
    MarketScan,
    SystemLog,
    Watchlist,
)

logger = get_logger("smartmoney.scan")


def _log(db: Session, level: str, message: str, source: str = "scan", **ctx) -> None:
    db.add(SystemLog(level=level, source=source, message=message, context=ctx))


def _upsert_asset(db: Session, r: AnalysisResult) -> Asset:
    asset = db.scalar(select(Asset).where(Asset.ticker == r.ticker))
    if asset is None:
        asset = Asset(ticker=r.ticker)
        db.add(asset)
    f = r.fundamentals
    asset.name = r.name
    asset.sector = f.sector
    asset.industry = f.industry
    asset.country = f.country
    asset.currency = f.currency
    asset.market_cap = f.market_cap
    asset.last_price = f.last_price
    asset.conviction_score = r.conviction_score
    asset.summary = r.summary
    asset.data_source = r.data_source
    asset.data_provider = r.data_provider
    db.flush()
    return asset


def _persist_result(db: Session, scan: MarketScan, r: AnalysisResult) -> InvestmentThesis:
    asset = _upsert_asset(db, r)

    thesis = InvestmentThesis(
        asset_id=asset.id,
        scan_id=scan.id,
        ticker=r.ticker,
        name=r.name,
        financial_score=r.financial_score,
        business_score=r.business_score,
        future_score=r.future_score,
        contrarian_score=r.contrarian_score,
        geopolitical_score=r.geopolitical_score,
        conviction_score=r.conviction_score,
        recommendation=r.recommendation,
        geo_risk=r.geo_risk,
        outlook=r.outlook,
        volatility=r.volatility,
        summary=r.summary,
        thesis=r.thesis,
        risks=r.risks,
        catalysts=r.catalysts,
        engine=r.engine,
        data_source=r.data_source,
        data_provider=r.data_provider,
    )
    db.add(thesis)
    db.flush()

    for out in r.agent_outputs:
        db.add(AgentReport(
            asset_id=asset.id,
            thesis_id=thesis.id,
            ticker=r.ticker,
            agent_role=out.role,
            score=out.score,
            summary=out.summary,
            payload=out.payload,
        ))

    db.add(HistoricalScore(
        asset_id=asset.id,
        ticker=r.ticker,
        conviction_score=r.conviction_score,
        financial_score=r.financial_score,
        business_score=r.business_score,
        future_score=r.future_score,
        contrarian_score=r.contrarian_score,
    ))

    # Keep watchlist conviction fresh.
    wl = db.scalar(select(Watchlist).where(Watchlist.ticker == r.ticker))
    if wl is not None:
        wl.conviction_score = r.conviction_score

    return thesis


def run_scan(scan_id: int, tickers: Optional[List[str]] = None,
             allow_network: bool = True) -> None:
    """Execute the scan identified by ``scan_id`` and persist every result."""
    db = SessionLocal()
    try:
        scan = db.get(MarketScan, scan_id)
        if scan is None:
            logger.error("Scan %s not found", scan_id)
            return

        universe = tickers or list(scan.requested_tickers or []) or settings.default_universe
        scan.status = "running"
        scan.started_at = datetime.now(timezone.utc)
        scan.requested_tickers = universe
        _log(db, "INFO", f"Scan #{scan_id} started for {len(universe)} tickers")
        db.commit()

        scores: List[float] = []
        for ticker in universe:
            try:
                result = analyze_asset(ticker, allow_network=allow_network)
                _persist_result(db, scan, result)
                scores.append(result.conviction_score)
                db.commit()
            except Exception as exc:  # per-ticker isolation
                db.rollback()
                logger.exception("Failed analysing %s", ticker)
                _log(db, "ERROR", f"Failed analysing {ticker}: {exc}", ticker=ticker)
                db.commit()

        scan = db.get(MarketScan, scan_id)
        scan.num_assets = len(scores)
        scan.avg_score = round(sum(scores) / len(scores), 2) if scores else None
        scan.status = "completed"
        scan.finished_at = datetime.now(timezone.utc)
        _log(db, "INFO", f"Scan #{scan_id} completed: {len(scores)} assets, avg={scan.avg_score}")
        db.commit()
    except Exception as exc:  # pragma: no cover - top-level guard
        db.rollback()
        logger.exception("Scan %s crashed", scan_id)
        scan = db.get(MarketScan, scan_id)
        if scan is not None:
            scan.status = "failed"
            scan.error = str(exc)
            scan.finished_at = datetime.now(timezone.utc)
            db.commit()
    finally:
        db.close()
