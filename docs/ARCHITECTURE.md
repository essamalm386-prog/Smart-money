# Architecture

## Overview

Smart Money is a two-tier application: a **FastAPI** backend that runs a
four-agent analysis pipeline, and a **React PWA** frontend that visualises the
ranked opportunities.

```
┌──────────────┐    HTTP /api    ┌────────────────────────────────────────┐
│  React PWA   │ ──────────────▶ │  FastAPI                               │
│ (Vite + RQ)  │ ◀────────────── │   ├─ api/        routers               │
│  service     │      JSON       │   ├─ agents/     CrewAI crew + engine   │
│  worker      │                 │   ├─ services/   yfinance, ddg, scoring │
└──────────────┘                 │   ├─ tasks/      scan runner            │
                                 │   ├─ models/     SQLAlchemy (7 tables)  │
                                 │   └─ schemas/    Pydantic v2            │
                                 │             │                          │
                                 │             ▼                          │
                                 │        SQLite (Alembic)                │
                                 └────────────────────────────────────────┘
```

## The analysis pipeline

`POST /api/scans` creates a `MarketScan` row and schedules `run_scan` as a
FastAPI background task. For each ticker:

1. **Market data** — `services/market_data.get_fundamentals` (yfinance, with a
   deterministic synthetic fallback when offline).
2. **Media signal** — `services/search.get_media_signal` (DuckDuckGo news, with
   fallback).
3. **Four agents** score the asset (`agents/analyzers.py`):
   - Quant Analyst → financial (30 %)
   - Business Analyst → business (25 %)
   - Future Potential → future (25 %)
   - Contrarian Analyst → contrarian (20 %)
4. **Investment Committee** combines them into a weighted conviction score,
   recommendation, and a thesis (`agents/pipeline.py`). If an Anthropic key is
   present, the CrewAI committee (`agents/crew.py`) writes the narrative.
5. Results are persisted: `assets`, `investment_theses`, `agent_reports`,
   `historical_scores`, plus `system_logs`.

## Graceful degradation

Every external dependency (yfinance, DuckDuckGo, CrewAI/Anthropic) is imported
lazily and guarded. With **no** API key and **no** network the whole system
still runs end-to-end on deterministic synthetic data — which is exactly what
the test-suite exercises.

## Data model

Seven tables: `assets`, `investment_theses`, `market_scans`, `agent_reports`,
`watchlists`, `system_logs`, `historical_scores`. See `docs/API.md` for the
relational summary and endpoints.
