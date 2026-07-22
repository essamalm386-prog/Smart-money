"""Pytest fixtures — isolated in-memory-style SQLite DB + TestClient."""
from __future__ import annotations

import os
import tempfile

import pytest

# Force offline / heuristic mode for deterministic, network-free tests.
os.environ.setdefault("USE_LLM", "false")
os.environ.setdefault("ANTHROPIC_API_KEY", "")
os.environ.setdefault("ENABLE_NETWORK", "false")

_tmp = tempfile.NamedTemporaryFile(suffix=".db", delete=False)
os.environ["DATABASE_URL"] = f"sqlite:///{_tmp.name}"


@pytest.fixture(scope="session", autouse=True)
def _init_database():
    from app.database import init_db
    init_db()
    yield


@pytest.fixture()
def client():
    from fastapi.testclient import TestClient
    from app.main import app

    with TestClient(app) as c:
        yield c


@pytest.fixture()
def db_session():
    from app.database.session import SessionLocal
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()
