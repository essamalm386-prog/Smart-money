"""Universe endpoints — sectors & investment themes to screen for opportunities."""
from __future__ import annotations

from fastapi import APIRouter

from app.services.universe import list_universe

router = APIRouter()


@router.get("")
def get_universe() -> dict:
    """Return the investable sectors and themes (with French labels)."""
    return list_universe()
