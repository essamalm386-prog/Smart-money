"""Pappers endpoints — French company registry search & detail."""
from __future__ import annotations

from fastapi import APIRouter, Query

from app.services import pappers as pappers_service

router = APIRouter()


@router.get("/status")
def pappers_status() -> dict:
    """Whether a Pappers API key is configured on the server."""
    return pappers_service.status()


@router.get("/search")
def pappers_search(q: str = Query(..., min_length=1, description="Nom ou SIREN")) -> dict:
    return pappers_service.search_companies(q)


@router.get("/company/{siren}")
def pappers_company(siren: str) -> dict:
    return pappers_service.get_company(siren)
