"""Deterministic analytical engine backing each CrewAI agent.

Each function turns raw inputs into a bounded sub-score (0-100) plus a short
natural-language rationale and a structured payload. This engine is what makes
Smart Money runnable and *testable* without any LLM: the CrewAI/LLM layer, when
enabled, enriches these results with narrative reasoning but the numeric spine
is deterministic and auditable.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Dict, List

from app.services.geopolitics import GeoSignal
from app.services.market_data import Fundamentals
from app.services.scoring import clamp
from app.services.search import MediaSignal


@dataclass(slots=True)
class AgentOutput:
    role: str
    score: float
    summary: str
    payload: Dict = field(default_factory=dict)


def _score_band(value, thresholds, points) -> float:
    """Map a numeric value to points via ascending thresholds."""
    if value is None:
        return points[len(points) // 2]  # neutral if unknown
    for t, p in zip(thresholds, points):
        if value <= t:
            return p
    return points[-1]


# --------------------------------------------------------------------------- #
# 1. QUANT ANALYST — financial quality
#    valuation, growth, cash-flow, debt, margins
# --------------------------------------------------------------------------- #
def quant_analysis(f: Fundamentals) -> AgentOutput:
    signals: List[str] = []

    # Valuation (cheaper P/E -> higher). Lower is better.
    valuation = _score_band(f.pe_ratio, [10, 18, 25, 35, 50], [100, 82, 62, 40, 22, 8])
    # Growth (revenue + earnings).
    rev = (f.revenue_growth or 0) * 100
    eps = (f.earnings_growth or 0) * 100
    growth = clamp(50 + rev * 1.3 + eps * 0.9)
    # Cash-flow: positive FCF rewarded.
    fcf = f.free_cashflow or 0
    cashflow = 78 if fcf > 0 else 32
    if fcf > 1e10:
        cashflow = 92
    # Debt: lower debt/equity better.
    debt = _score_band(f.debt_to_equity, [30, 70, 120, 180, 260], [95, 80, 60, 42, 25, 12])
    # Margins: operating margin.
    margins = clamp(40 + (f.operating_margins or 0) * 160)

    score = round(
        0.25 * valuation + 0.22 * growth + 0.20 * cashflow + 0.18 * debt + 0.15 * margins, 2
    )

    if valuation >= 62:
        signals.append("attractive valuation multiple")
    if growth >= 60:
        signals.append("solid top/bottom-line growth")
    if cashflow >= 78:
        signals.append("positive free cash-flow")
    if debt < 45:
        signals.append("elevated leverage")
    if margins >= 60:
        signals.append("healthy operating margins")

    summary = (
        f"Financial quality {score:.0f}/100 — "
        + (", ".join(signals) if signals else "balanced fundamentals")
        + "."
    )
    return AgentOutput(
        role="Quant Analyst",
        score=clamp(score),
        summary=summary,
        payload={
            "valuation": valuation, "growth": round(growth, 1), "cashflow": cashflow,
            "debt": debt, "margins": round(margins, 1),
            "pe_ratio": f.pe_ratio, "revenue_growth": f.revenue_growth,
            "debt_to_equity": f.debt_to_equity, "free_cashflow": f.free_cashflow,
        },
    )


# --------------------------------------------------------------------------- #
# 2. BUSINESS ANALYST — moat, market, management, competitiveness
# --------------------------------------------------------------------------- #
def business_analysis(f: Fundamentals) -> AgentOutput:
    signals: List[str] = []
    # Moat proxy: gross margin durability.
    moat = clamp(35 + (f.gross_margins or 0) * 90)
    # Market: scale (market cap) as a rough competitive-position proxy.
    cap = f.market_cap or 0
    if cap >= 1e12:
        market = 88
    elif cap >= 1e11:
        market = 78
    elif cap >= 1e10:
        market = 66
    elif cap >= 1e9:
        market = 54
    else:
        market = 40
    # Management: capital efficiency (ROE).
    management = clamp(45 + (f.return_on_equity or 0) * 130)
    # Competitiveness: profit margin vs peers proxy.
    competitiveness = clamp(40 + (f.profit_margins or 0) * 170)

    score = round(0.30 * moat + 0.25 * market + 0.25 * management + 0.20 * competitiveness, 2)

    if moat >= 70:
        signals.append("wide-moat gross margins")
    if market >= 75:
        signals.append("dominant market scale")
    if management >= 65:
        signals.append("efficient capital allocation")
    if competitiveness >= 65:
        signals.append("strong competitive profitability")

    summary = (
        f"Business quality {score:.0f}/100 — "
        + (", ".join(signals) if signals else "average competitive position")
        + "."
    )
    return AgentOutput(
        role="Business Analyst",
        score=clamp(score),
        summary=summary,
        payload={
            "moat": round(moat, 1), "market": market,
            "management": round(management, 1), "competitiveness": round(competitiveness, 1),
            "gross_margins": f.gross_margins, "return_on_equity": f.return_on_equity,
            "market_cap": f.market_cap,
        },
    )


# --------------------------------------------------------------------------- #
# 3. FUTURE POTENTIAL — growth runway, forward valuation, analyst trajectory
# --------------------------------------------------------------------------- #
def future_analysis(f: Fundamentals) -> AgentOutput:
    signals: List[str] = []
    rev = (f.revenue_growth or 0) * 100
    runway = clamp(45 + rev * 1.8)
    # PEG: growth-adjusted valuation; <1 excellent.
    peg = _score_band(f.peg_ratio, [1.0, 1.5, 2.0, 3.0, 4.0], [96, 80, 62, 45, 28, 15])
    # Forward vs trailing PE improvement.
    if f.forward_pe and f.pe_ratio and f.forward_pe < f.pe_ratio:
        forward = 78
        signals.append("earnings expected to expand")
    else:
        forward = 50
    score = round(0.45 * runway + 0.35 * peg + 0.20 * forward, 2)

    if runway >= 65:
        signals.append("strong revenue runway")
    if peg >= 80:
        signals.append("cheap on a growth-adjusted basis")

    summary = (
        f"Future potential {score:.0f}/100 — "
        + (", ".join(signals) if signals else "moderate growth outlook")
        + "."
    )
    return AgentOutput(
        role="Future Potential",
        score=clamp(score),
        summary=summary,
        payload={"runway": round(runway, 1), "peg": peg, "forward": forward,
                 "peg_ratio": f.peg_ratio, "revenue_growth": f.revenue_growth},
    )


# --------------------------------------------------------------------------- #
# 4. CONTRARIAN ANALYST — media saturation, crowdedness, sentiment
#    Core edge: reward quiet, under-covered quality; penalise crowded hype.
# --------------------------------------------------------------------------- #
def contrarian_analysis(f: Fundamentals, media: MediaSignal) -> AgentOutput:
    signals: List[str] = []
    # Under-coverage is good: invert saturation.
    coverage = clamp((1.0 - media.saturation) * 100)
    # Analyst crowding: recommendationMean 1=StrongBuy..5=Sell.
    # A crowded StrongBuy (everyone already bullish) is less contrarian.
    rec = f.recommendation_mean
    if rec is None:
        crowding = 55.0
    else:
        # mean around 2.5-3 (lukewarm street) is the contrarian sweet spot.
        crowding = clamp(100 - abs(rec - 2.8) * 40)
    score = round(0.65 * coverage + 0.35 * crowding, 2)

    if coverage >= 65:
        signals.append("under the radar / low media saturation")
    else:
        signals.append("already widely covered")
    if rec is not None and rec > 2.8:
        signals.append("street still cautious (room to re-rate)")

    summary = (
        f"Contrarian edge {score:.0f}/100 — "
        + ", ".join(signals)
        + f" ({media.article_count} recent articles)."
    )
    return AgentOutput(
        role="Contrarian Analyst",
        score=clamp(score),
        summary=summary,
        payload={
            "coverage": round(coverage, 1), "crowding": round(crowding, 1),
            "saturation": media.saturation, "article_count": media.article_count,
            "headlines": media.headlines,
        },
    )


# --------------------------------------------------------------------------- #
# 5. GEOPOLITICAL ANALYST — country risk, sector exposure, conflict/sanctions
#    Anticipates future fluctuations driven by geostrategic forces.
# --------------------------------------------------------------------------- #
def geopolitical_analysis(geo: GeoSignal) -> AgentOutput:
    summary = (
        f"Perspective géopolitique {geo.geo_score:.0f}/100 — risque {geo.risk_level.lower()} : "
        + " ; ".join(geo.drivers[:3])
        + "."
    )
    if geo.headlines:
        summary += " Titres : " + " · ".join(geo.headlines[:2])
    return AgentOutput(
        role="Analyste Géopolitique",
        score=clamp(geo.geo_score),
        summary=summary,
        payload={
            "risk_level": geo.risk_level,
            "tension": geo.tension,
            "avg_tone": geo.avg_tone,
            "country": geo.country,
            "sector": geo.sector,
            "drivers": geo.drivers,
            "headlines": geo.headlines,
            "source": geo.source,
        },
    )
