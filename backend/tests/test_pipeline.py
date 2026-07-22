"""Tests for the analytical pipeline (offline / deterministic)."""
from __future__ import annotations

from app.agents.pipeline import analyze_asset


def test_analyze_asset_is_deterministic():
    a = analyze_asset("AAPL", allow_network=False)
    b = analyze_asset("AAPL", allow_network=False)
    assert a.conviction_score == b.conviction_score
    assert a.recommendation == b.recommendation


def test_analyze_asset_scores_in_range():
    r = analyze_asset("MSFT", allow_network=False)
    for s in (r.financial_score, r.business_score, r.future_score,
              r.contrarian_score, r.geopolitical_score, r.conviction_score):
        assert 0 <= s <= 100
    assert len(r.agent_outputs) == 5
    roles = {o.role for o in r.agent_outputs}
    assert roles == {
        "Quant Analyst", "Business Analyst", "Future Potential",
        "Contrarian Analyst", "Analyste Géopolitique",
    }


def test_conviction_matches_weighting():
    r = analyze_asset("NVDA", allow_network=False)
    expected = (
        0.25 * r.financial_score
        + 0.20 * r.business_score
        + 0.20 * r.future_score
        + 0.15 * r.contrarian_score
        + 0.20 * r.geopolitical_score
    )
    assert abs(r.conviction_score - expected) < 0.05


def test_geopolitical_forecast_present():
    r = analyze_asset("XOM", allow_network=False)
    assert r.geo_risk in {"Faible", "Modéré", "Élevé"}
    assert r.outlook in {"Haussière", "Neutre", "Prudente"}
    assert r.volatility in {"Faible", "Modérée", "Élevée"}


def test_gdelt_offline_returns_none():
    # Offline / no network → GDELT gracefully yields None (never raises).
    from app.services.gdelt import get_geopolitical_news
    assert get_geopolitical_news("Renault sanctions", allow_network=False) is None


def test_engine_is_heuristic_without_key():
    r = analyze_asset("TSLA", allow_network=False)
    assert r.engine == "heuristic"
    assert r.thesis and r.summary


def test_offline_data_is_flagged_simulated():
    r = analyze_asset("AAPL", allow_network=False)
    assert r.data_source == "simulated"
    assert r.data_provider == "Simulation"
