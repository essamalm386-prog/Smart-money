# API Reference

Base URL: `/api`  · Interactive docs: `/docs` (Swagger) and `/redoc`.

| Method | Path                    | Description |
|--------|-------------------------|-------------|
| GET    | `/health`               | Liveness + version |
| GET    | `/assets`               | Assets ranked by conviction (`?sector=`, `?limit=`) |
| GET    | `/assets/{ticker}`      | Asset + latest thesis + agent reports + score history |
| GET    | `/theses`               | Ranked opportunity list (`?limit=`, `?latest_per_ticker=`) |
| GET    | `/theses/{id}`          | Full thesis + 4 agent reports |
| POST   | `/scans`                | Launch a scan `{tickers:[], label}` → runs in background |
| GET    | `/scans`                | Scan history |
| GET    | `/scans/{id}`           | Scan + its ranked theses |
| GET    | `/watchlist`            | Watchlist items |
| POST   | `/watchlist`            | Add `{ticker, note}` |
| DELETE | `/watchlist/{ticker}`   | Remove |
| GET    | `/reports`              | Agent report feed (`?agent_role=`, `?ticker=`) |
| GET    | `/config`               | Runtime config + whether Anthropic key is set |
| PUT    | `/config`               | Update scan parameters |
| GET    | `/logs`                 | Structured system logs (`?level=`) |

## Relational schema

```
assets 1───∞ investment_theses ∞───1 market_scans
   │                  │
   │                  ∞
   │             agent_reports
   ├───∞ historical_scores
watchlists (standalone, keyed by ticker)
system_logs (standalone)
```

## Example: run a scan

```bash
curl -X POST localhost:8000/api/scans \
  -H 'Content-Type: application/json' \
  -d '{"tickers":["AAPL","MSFT","NVDA"],"label":"tech scan"}'

curl "localhost:8000/api/theses?limit=5"     # ranked opportunities
```
