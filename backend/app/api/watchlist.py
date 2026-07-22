"""Watchlist endpoints."""
from __future__ import annotations

from typing import List

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import desc, select
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Asset, Watchlist
from app.schemas import WatchlistCreate, WatchlistOut

router = APIRouter()


@router.get("", response_model=List[WatchlistOut])
def list_watchlist(db: Session = Depends(get_db)):
    return list(db.scalars(select(Watchlist).order_by(desc(Watchlist.created_at))))


@router.post("", response_model=WatchlistOut, status_code=201)
def add_watchlist(payload: WatchlistCreate, db: Session = Depends(get_db)) -> Watchlist:
    existing = db.scalar(select(Watchlist).where(Watchlist.ticker == payload.ticker))
    if existing is not None:
        if payload.note is not None:
            existing.note = payload.note
        db.commit()
        db.refresh(existing)
        return existing

    asset = db.scalar(select(Asset).where(Asset.ticker == payload.ticker))
    item = Watchlist(
        ticker=payload.ticker,
        note=payload.note,
        name=asset.name if asset else None,
        conviction_score=asset.conviction_score if asset else None,
    )
    db.add(item)
    db.commit()
    db.refresh(item)
    return item


@router.delete("/{ticker}")
def remove_watchlist(ticker: str, db: Session = Depends(get_db)) -> dict:
    item = db.scalar(select(Watchlist).where(Watchlist.ticker == ticker.upper()))
    if item is None:
        raise HTTPException(status_code=404, detail="Not on watchlist")
    db.delete(item)
    db.commit()
    return {"ok": True}
