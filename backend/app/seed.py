"""Seed the database with a demo scan so the dashboard has data on first run.

Usage:  python -m app.seed
"""
from __future__ import annotations

from app.config import settings
from app.database import init_db
from app.database.session import SessionLocal
from app.logging_config import configure_logging, get_logger
from app.models import MarketScan
from app.tasks import run_scan

logger = get_logger("smartmoney.seed")


def main() -> None:
    configure_logging(settings.log_level)
    init_db()
    db = SessionLocal()
    try:
        scan = MarketScan(
            label="Seed scan",
            status="pending",
            requested_tickers=list(settings.default_universe),
        )
        db.add(scan)
        db.commit()
        scan_id = scan.id
    finally:
        db.close()

    logger.info("Running seed scan #%s ...", scan_id)
    run_scan(scan_id, list(settings.default_universe), allow_network=True)
    logger.info("Seed complete.")


if __name__ == "__main__":
    main()
