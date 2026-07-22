"""Runtime configuration endpoints.

Config is held in a small in-memory overlay on top of the immutable settings so
the UI can tune scan parameters without a restart. (For a single-node MVP this
is intentionally simple; persist to DB if horizontal scaling is required.)
"""
from __future__ import annotations

from app.config import settings
from app.schemas import ConfigOut, ConfigUpdate
from fastapi import APIRouter

router = APIRouter()

# Mutable runtime overlay seeded from static settings.
_runtime = {
    "min_market_cap": settings.min_market_cap,
    "excluded_sectors": list(settings.excluded_sectors),
    "max_pe": settings.max_pe,
    "scan_universe": list(settings.default_universe),
    "use_llm": settings.use_llm,
}


def current_config() -> ConfigOut:
    return ConfigOut(
        min_market_cap=_runtime["min_market_cap"],
        excluded_sectors=_runtime["excluded_sectors"],
        max_pe=_runtime["max_pe"],
        scan_universe=_runtime["scan_universe"],
        use_llm=_runtime["use_llm"],
        anthropic_configured=settings.anthropic_configured,
        pappers_configured=settings.pappers_configured,
        llm_model=settings.llm_model,
    )


@router.get("", response_model=ConfigOut)
def get_config() -> ConfigOut:
    return current_config()


@router.put("", response_model=ConfigOut)
def update_config(payload: ConfigUpdate) -> ConfigOut:
    data = payload.model_dump(exclude_none=True)
    _runtime.update(data)
    return current_config()
