"""Conviction scoring engine.

The final conviction score (0-100) is a weighted blend of four sub-scores, each
independently normalised to 0-100:

    CONVICTION = 0.30 * FINANCIAL
               + 0.25 * BUSINESS
               + 0.25 * FUTURE
               + 0.20 * CONTRARIAN

Weights sum to 1.00. Each sub-score is produced by a dedicated analyst and is
itself a bounded, deterministic function of fundamental / sentiment inputs so
the system is fully testable without any external LLM.
"""
from __future__ import annotations

from dataclasses import dataclass, asdict
from typing import Dict

# --- Canonical weights (must sum to 1.0) -----------------------------------
# v2 model: geopolitics / geostrategy added as a 5th factor to anticipate future
# fluctuations driven by conflicts, sanctions, supply-chain and country risk.
WEIGHTS: Dict[str, float] = {
    "financial": 0.25,
    "business": 0.20,
    "future": 0.20,
    "contrarian": 0.15,
    "geopolitics": 0.20,
}

assert abs(sum(WEIGHTS.values()) - 1.0) < 1e-9, "Scoring weights must sum to 1.0"


def clamp(value: float, low: float = 0.0, high: float = 100.0) -> float:
    """Clamp a value into the [low, high] range."""
    return max(low, min(high, value))


@dataclass(slots=True)
class SubScores:
    financial: float
    business: float
    future: float
    contrarian: float
    geopolitics: float = 50.0

    def clamped(self) -> "SubScores":
        return SubScores(
            financial=clamp(self.financial),
            business=clamp(self.business),
            future=clamp(self.future),
            contrarian=clamp(self.contrarian),
            geopolitics=clamp(self.geopolitics),
        )

    def as_dict(self) -> Dict[str, float]:
        return asdict(self)


def conviction_score(sub: SubScores) -> float:
    """Compute the final weighted conviction score (0-100)."""
    s = sub.clamped()
    score = (
        WEIGHTS["financial"] * s.financial
        + WEIGHTS["business"] * s.business
        + WEIGHTS["future"] * s.future
        + WEIGHTS["contrarian"] * s.contrarian
        + WEIGHTS["geopolitics"] * s.geopolitics
    )
    return round(clamp(score), 2)


def recommendation_for(score: float) -> str:
    """Map a conviction score to a discrete recommendation label."""
    if score >= 80:
        return "STRONG_BUY"
    if score >= 65:
        return "BUY"
    if score >= 45:
        return "HOLD"
    if score >= 30:
        return "REDUCE"
    return "AVOID"


def quality_gate(market_cap: float | None, pe: float | None,
                 min_market_cap: float, max_pe: float) -> bool:
    """Return True if the asset passes the minimum-quality filter.

    Filters out 'mediocre' assets before deep analysis: sub-scale market cap
    or an implausibly stretched P/E multiple.
    """
    if market_cap is not None and market_cap < min_market_cap:
        return False
    if pe is not None and pe > 0 and pe > max_pe:
        return False
    return True
