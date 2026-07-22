"""API routers package."""
from fastapi import APIRouter

from app.api import (
    assets, config, health, pappers, reports, scans, system, theses, universe, watchlist,
)

api_router = APIRouter()
api_router.include_router(health.router, tags=["health"])
api_router.include_router(universe.router, prefix="/universe", tags=["universe"])
api_router.include_router(pappers.router, prefix="/pappers", tags=["pappers"])
api_router.include_router(assets.router, prefix="/assets", tags=["assets"])
api_router.include_router(theses.router, prefix="/theses", tags=["theses"])
api_router.include_router(scans.router, prefix="/scans", tags=["scans"])
api_router.include_router(watchlist.router, prefix="/watchlist", tags=["watchlist"])
api_router.include_router(reports.router, prefix="/reports", tags=["reports"])
api_router.include_router(config.router, prefix="/config", tags=["config"])
api_router.include_router(system.router, prefix="/logs", tags=["system"])

__all__ = ["api_router"]
