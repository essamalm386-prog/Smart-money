"""Geopolitical / geostrategic analysis.

Estimates how exposed an asset is to geopolitical forces (country risk, sector
sensitivity to conflict / sanctions / supply chains, and live geopolitical news
tension) in order to anticipate FUTURE fluctuations. Produces:

- ``geo_score`` (0-100): higher = more geopolitically favorable / resilient.
- ``risk_level`` (Faible | Modéré | Élevé): headline risk read.
- ``drivers``: short French bullet reasons.

Live tension comes from DuckDuckGo news; a deterministic synthetic signal is used
when the network / search library is unavailable so scans always complete.
"""
from __future__ import annotations

import hashlib
from dataclasses import dataclass, field
from typing import List, Optional

from app.logging_config import get_logger
from app.services.gdelt import get_geopolitical_news
from app.services.scoring import clamp
from app.services.search import _DDG_AVAILABLE, DDGS  # reuse DDG availability

logger = get_logger("smartmoney.geopolitics")

# Country stability (0-100, higher = more stable / lower political risk).
COUNTRY_STABILITY = {
    "United States": 78, "United States of America": 78, "USA": 78,
    "France": 75, "Germany": 77, "United Kingdom": 74, "Netherlands": 80,
    "Switzerland": 88, "Canada": 82, "Japan": 80, "South Korea": 66,
    "Taiwan": 52, "China": 55, "Hong Kong": 58, "India": 62, "Brazil": 58,
    "Mexico": 57, "Israel": 55, "Saudi Arabia": 56, "Russia": 30, "Ukraine": 28,
    "Ireland": 82, "Sweden": 84, "Denmark": 85, "Australia": 82,
}
DEFAULT_STABILITY = 62

# Sectors highly sensitive to geopolitics (both risk AND opportunity).
SECTOR_SENSITIVITY = {
    "Energy": 0.9, "Énergie": 0.9,
    "Technology": 0.7, "Technologie": 0.7,
    "Semiconductors": 0.95,
    "Basic Materials": 0.8, "Materials": 0.8, "Matériaux": 0.8,
    "Industrials": 0.7, "Industrie": 0.7,
    "Aerospace & Defense": 0.9, "Defense": 0.9, "Défense": 0.9,
    "Utilities": 0.6, "Services publics": 0.6,
    "Financial Services": 0.5, "Finance": 0.5,
    "Healthcare": 0.4, "Santé": 0.4,
    "Consumer Defensive": 0.4, "Consumer Cyclical": 0.5,
}

# Sectors that structurally BENEFIT from current geostrategic trends
# (reshoring, energy security, defense spending, chip sovereignty).
SECTOR_TAILWIND = {
    "Energy": 8, "Énergie": 8, "Semiconductors": 10, "Technology": 5,
    "Aerospace & Defense": 10, "Defense": 10, "Défense": 10,
    "Basic Materials": 6, "Materials": 6, "Matériaux": 6, "Industrials": 4,
}

# Tension keywords used to weight geopolitical news relevance.
_TENSION_TERMS = (
    "sanctions", "tariff", "tariffs", "war", "conflict", "export ban",
    "supply chain", "geopolitical", "guerre", "sanction", "embargo",
)


@dataclass(slots=True)
class GeoSignal:
    ticker: str
    country: Optional[str]
    sector: Optional[str]
    geo_score: float
    risk_level: str
    tension: float  # 0..1 live news tension (1 = very tense)
    drivers: List[str] = field(default_factory=list)
    headlines: List[str] = field(default_factory=list)
    avg_tone: Optional[float] = None
    source: str = "synthetic"


def _seed(text: str) -> float:
    digest = hashlib.sha256(("geo" + text).encode("utf-8")).hexdigest()
    return int(digest[:8], 16) / 0xFFFFFFFF


def _risk_level(score: float) -> str:
    if score >= 66:
        return "Faible"
    if score >= 45:
        return "Modéré"
    return "Élevé"


def _synthetic_tension(ticker: str, country: Optional[str]) -> float:
    return round(_seed(ticker + (country or "")), 3)


def _live_tension(query: str) -> Optional[float]:  # pragma: no cover - network
    if not _DDG_AVAILABLE:
        return None
    try:
        with DDGS() as ddgs:  # type: ignore[operator]
            results = list(ddgs.news(query, max_results=20))
        if not results:
            return 0.2
        hits = 0
        for r in results:
            text = f"{r.get('title', '')} {r.get('body', '')}".lower()
            if any(term in text for term in _TENSION_TERMS):
                hits += 1
        return round(min(1.0, hits / 10.0), 3)
    except Exception as exc:
        logger.debug("geo news failed: %s", exc)
        return None


def analyze_geopolitics(ticker: str, country: Optional[str], sector: Optional[str],
                        name: Optional[str] = None, allow_network: bool = True) -> GeoSignal:
    """Compute the geopolitical signal for an asset."""
    stability = COUNTRY_STABILITY.get((country or "").strip(), DEFAULT_STABILITY)
    sensitivity = SECTOR_SENSITIVITY.get((sector or "").strip(), 0.6)
    tailwind = SECTOR_TAILWIND.get((sector or "").strip(), 0)

    source = "synthetic"
    tension: Optional[float] = None
    headlines: List[str] = []
    avg_tone: Optional[float] = None
    query = f'"{name or ticker}" (sanctions OR tariffs OR conflict OR geopolitical)'

    if allow_network:
        # 1) GDELT — real global news monitoring (free, no key).
        gd = get_geopolitical_news(query)
        if gd and gd.get("available"):
            tension = gd.get("tension")
            headlines = gd.get("headlines") or []
            avg_tone = gd.get("avg_tone")
            source = "GDELT"
        # 2) DuckDuckGo news fallback.
        if tension is None:
            tension = _live_tension(
                f"{name or ticker} {country or ''} sanctions tariffs geopolitics"
            )
            if tension is not None:
                source = "duckduckgo"
    # 3) Deterministic synthetic fallback.
    if tension is None:
        tension = _synthetic_tension(ticker, country)

    # More sensitive sectors are more affected by tension (positive or negative).
    tension_penalty = tension * 100 * sensitivity
    geo_score = clamp(0.55 * stability + 0.30 * (100 - tension_penalty) + tailwind + 0.15 * 50)
    # (the +0.15*50 keeps a neutral baseline so scores centre reasonably)
    geo_score = round(clamp(geo_score), 2)
    level = _risk_level(geo_score)

    drivers: List[str] = []
    drivers.append(f"Stabilité pays ({country or 'n/d'}) : {stability}/100")
    if sensitivity >= 0.8:
        drivers.append("Secteur très exposé aux tensions géopolitiques")
    if tailwind >= 6:
        drivers.append("Secteur porté par les tendances géostratégiques (souveraineté, défense, énergie)")
    if tension >= 0.5:
        drivers.append("Actualité géopolitique tendue récemment")
    elif tension <= 0.2:
        drivers.append("Environnement géopolitique calme")

    if source == "GDELT":
        drivers.append("Signal d'actualité mondial GDELT (temps quasi réel)")

    return GeoSignal(
        ticker=ticker, country=country, sector=sector,
        geo_score=geo_score, risk_level=level, tension=tension,
        drivers=drivers, headlines=headlines, avg_tone=avg_tone, source=source,
    )
