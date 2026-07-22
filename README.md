# 💰 Smart Money

A Progressive Web App that **surfaces investment opportunities before they
become mainstream**. A four-agent CrewAI committee scans the market, filters out
mediocre assets, detects positive anomalies, reads the media/sentiment tape, and
produces a ranked list of high-conviction theses with a professional dashboard.

> **Runs out of the box with zero API keys.** Without an Anthropic key or
> network access, the deterministic scoring engine + synthetic data provide a
> fully working system. Add a key to enable LLM narrative synthesis.

---

## Stack

**Backend** — Python 3.11, FastAPI, Uvicorn, SQLAlchemy 2, Alembic, SQLite,
Pydantic v2, CrewAI, Anthropic, yfinance, DuckDuckGo Search.

**Frontend** — React 18, Vite, TailwindCSS, React Query, Lucide, PWA
(manifest + service worker + offline cache + push-ready).

**DevOps** — Docker, Docker Compose, `.env`, structured JSON logs.

---

## Quick start

### Option A — Docker (everything)

```bash
cp backend/.env.example backend/.env      # optionally add ANTHROPIC_API_KEY
cd deployment && docker compose up --build
# frontend → http://localhost:3000   backend → http://localhost:8000/docs
```

### Option B — Local dev

```bash
# 1. Backend
cd backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt           # or requirements-core.txt for the light stack
cp .env.example .env
alembic upgrade head
python -m app.seed                         # optional: populate demo data
uvicorn app.main:app --reload --port 8000

# 2. Frontend (new terminal)
cd frontend
npm install
npm run dev                                # http://localhost:5173 (proxies /api → :8000)
```

### Run the tests

```bash
cd backend && pytest          # 25 tests, deterministic, no network needed
```

---

## The four agents

1. **Quant Analyst** — valuation, growth, cash-flow, debt, margins → *financial (30 %)*
2. **Business Analyst** — moat, market, management, competitiveness → *business (25 %)*
3. **Contrarian Analyst** — news, media & social saturation → *contrarian (20 %)*
4. **Investment Committee** — final score, synthesis, ranking, storage

Plus a **Future Potential** dimension (growth runway / PEG) → *future (25 %)*.

### Conviction score

```
CONVICTION = 0.30·FINANCIAL + 0.25·BUSINESS + 0.25·FUTURE + 0.20·CONTRARIAN
```

Full model in [`docs/SCORING.md`](docs/SCORING.md).

---

## Screens

Dashboard · Asset detail · Scan history · Watchlist · Reports · Configuration —
all responsive, mobile-first, installable as a PWA, and resilient when the API
is offline.

---

## Documentation

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — system design & data flow
- [`docs/API.md`](docs/API.md) — endpoint & schema reference
- [`docs/SCORING.md`](docs/SCORING.md) — the conviction model

---

## Project layout

```
backend/    FastAPI app, CrewAI agents, services, models, Alembic, tests
frontend/   React PWA (Vite + Tailwind + React Query)
deployment/ docker-compose.yml
docs/       architecture, API, scoring
```
