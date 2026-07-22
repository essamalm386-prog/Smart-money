"""Smart Money FastAPI application entry point."""
from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api import api_router
from app.config import settings
from app.database import init_db
from app.logging_config import configure_logging, get_logger

configure_logging(settings.log_level)
logger = get_logger("smartmoney.app")


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting %s v%s (env=%s, llm=%s)",
                settings.app_name, settings.app_version,
                settings.environment, settings.llm_enabled)
    # Dev convenience: ensure tables exist. Production relies on Alembic.
    init_db()
    yield
    logger.info("Shutting down %s", settings.app_name)


app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="Detect investment opportunities before they become mainstream.",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):  # pragma: no cover
    logger.exception("Unhandled error on %s %s", request.method, request.url.path)
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error", "path": request.url.path},
    )


app.include_router(api_router, prefix=settings.api_prefix)


@app.get("/")
def root() -> dict:
    return {
        "app": settings.app_name,
        "version": settings.app_version,
        "docs": "/docs",
        "api": settings.api_prefix,
    }
