"""API integration tests using FastAPI TestClient."""
from __future__ import annotations


def test_health(client):
    r = client.get("/api/health")
    assert r.status_code == 200
    assert r.json()["status"] == "ok"


def test_root(client):
    r = client.get("/")
    assert r.status_code == 200
    assert r.json()["api"] == "/api"


def test_config_roundtrip(client):
    r = client.get("/api/config")
    assert r.status_code == 200
    body = r.json()
    assert "min_market_cap" in body and "anthropic_configured" in body

    upd = client.put("/api/config", json={"max_pe": 99})
    assert upd.status_code == 200
    assert upd.json()["max_pe"] == 99


def test_scan_flow_creates_ranked_theses(client):
    # Create a scan (runs synchronously via BackgroundTasks in TestClient).
    r = client.post("/api/scans", json={"tickers": ["AAPL", "MSFT", "NVDA"], "label": "test"})
    assert r.status_code == 201
    scan_id = r.json()["id"]

    detail = client.get(f"/api/scans/{scan_id}")
    assert detail.status_code == 200
    scan = detail.json()["scan"]
    assert scan["status"] in {"completed", "running", "pending"}

    theses = client.get("/api/theses").json()
    assert isinstance(theses, list)
    if len(theses) >= 2:
        # Ensure ranking is descending by conviction.
        scores = [t["conviction_score"] for t in theses]
        assert scores == sorted(scores, reverse=True)


def test_assets_and_detail(client):
    client.post("/api/scans", json={"tickers": ["GOOGL"], "label": "detail-test"})
    assets = client.get("/api/assets").json()
    assert any(a["ticker"] == "GOOGL" for a in assets)

    d = client.get("/api/assets/GOOGL")
    assert d.status_code == 200
    body = d.json()
    assert body["asset"]["ticker"] == "GOOGL"
    assert body["latest_thesis"] is not None
    assert len(body["reports"]) == 5


def test_watchlist_crud(client):
    add = client.post("/api/watchlist", json={"ticker": "amd", "note": "chip cycle"})
    assert add.status_code == 201
    assert add.json()["ticker"] == "AMD"

    lst = client.get("/api/watchlist").json()
    assert any(w["ticker"] == "AMD" for w in lst)

    rm = client.delete("/api/watchlist/AMD")
    assert rm.status_code == 200 and rm.json()["ok"] is True


def test_reports_feed(client):
    client.post("/api/scans", json={"tickers": ["META"]})
    reports = client.get("/api/reports", params={"agent_role": "Quant Analyst"}).json()
    assert all(r["agent_role"] == "Quant Analyst" for r in reports)


def test_thesis_not_found(client):
    assert client.get("/api/theses/999999").status_code == 404


def test_universe_lists_sectors_and_domains(client):
    u = client.get("/api/universe").json()
    assert "sectors" in u and "domains" in u
    assert any(s["key"] == "technologie" for s in u["sectors"])
    assert any(d["key"] == "ia" for d in u["domains"])
    # every entry has a French label and a non-empty ticker list
    for entry in u["sectors"] + u["domains"]:
        assert entry["label"] and entry["count"] >= 1


def test_scan_by_sector_expands_tickers(client):
    r = client.post("/api/scans", json={"sector": "energie"})
    assert r.status_code == 201
    body = r.json()
    assert body["label"].startswith("Secteur")
    assert "XOM" in body["requested_tickers"]


def test_scan_by_domain_expands_tickers(client):
    r = client.post("/api/scans", json={"domain": "ia"})
    assert r.status_code == 201
    assert r.json()["label"].startswith("Thématique")


def test_pappers_status_and_graceful_without_key(client):
    # No key configured in tests → endpoints must degrade gracefully, not crash.
    st = client.get("/api/pappers/status").json()
    assert st["configured"] is False
    search = client.get("/api/pappers/search", params={"q": "Renault"}).json()
    assert search["available"] is False
    assert "Pappers" in search["reason"] or "PAPPERS" in search["reason"]
    company = client.get("/api/pappers/company/441617895").json()
    assert company["available"] is False


def test_config_exposes_pappers_flag(client):
    body = client.get("/api/config").json()
    assert "pappers_configured" in body


def test_theses_carry_data_source(client):
    client.post("/api/scans", json={"tickers": ["AAPL"]})
    theses = client.get("/api/theses").json()
    assert theses, "expected at least one thesis"
    assert theses[0]["data_source"] in {"real", "simulated"}
    assert theses[0]["data_provider"]
