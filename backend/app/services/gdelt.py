"""GDELT integration — real global geopolitical news signal (free, no key).

GDELT DOC 2.0 API (https://api.gdeltproject.org/api/v2/doc/doc) monitors world
news in near real-time. We use it to measure geopolitical *tension* around an
asset/country: article volume on conflict/sanctions topics plus average media
tone (negative tone = more tension). No API key required.

Everything is guarded: if the network or ``requests`` is unavailable, returns
None so callers fall back to another source.
"""
from __future__ import annotations

from typing import Any, Dict, List, Optional

from app.logging_config import get_logger

logger = get_logger("smartmoney.gdelt")

try:  # pragma: no cover - import guard
    import requests  # type: ignore
    _REQUESTS_AVAILABLE = True
except Exception:  # pragma: no cover
    requests = None  # type: ignore
    _REQUESTS_AVAILABLE = False

_BASE = "https://api.gdeltproject.org/api/v2/doc/doc"
_HEADERS = {"User-Agent": "SmartMoney/1.0 (+geopolitics)"}


def _doc(params: Dict[str, Any]) -> Optional[dict]:  # pragma: no cover - network
    resp = requests.get(_BASE, params={**params, "format": "json"},
                        headers=_HEADERS, timeout=8)
    resp.raise_for_status()
    # GDELT sometimes returns empty body for no results.
    if not resp.text.strip():
        return {}
    return resp.json()


def get_geopolitical_news(query: str, timespan: str = "1week",
                          maxrecords: int = 75,
                          allow_network: bool = True) -> Optional[Dict[str, Any]]:
    """Return a geopolitical news signal for ``query`` or None if unavailable.

    Result: {available, source:"GDELT", article_count, avg_tone, tension (0..1),
    headlines: [...]}. ``tension`` blends coverage volume and negative tone.
    """
    if not _REQUESTS_AVAILABLE or not allow_network or not query.strip():
        return None
    try:  # pragma: no cover - network path
        art = _doc({"query": query, "mode": "artlist",
                    "timespan": timespan, "maxrecords": maxrecords}) or {}
        articles: List[dict] = art.get("articles") or []
        count = len(articles)
        headlines = [a.get("title") for a in articles[:5] if a.get("title")]

        avg_tone: Optional[float] = None
        try:
            tl = _doc({"query": query, "mode": "timelinetone", "timespan": timespan}) or {}
            series = (tl.get("timeline") or [{}])[0].get("data") or []
            vals = [p.get("value") for p in series
                    if isinstance(p.get("value"), (int, float))]
            if vals:
                avg_tone = sum(vals) / len(vals)
        except Exception as exc:
            logger.debug("GDELT tone failed: %s", exc)

        volume = min(1.0, count / float(maxrecords or 75))
        # GDELT tone typically ranges ~[-10, +10]; negative = tense.
        negativity = 0.0 if avg_tone is None else max(0.0, min(1.0, (-avg_tone) / 8.0))
        tension = round(min(1.0, 0.6 * volume + 0.4 * negativity), 3)

        return {
            "available": True,
            "source": "GDELT",
            "article_count": count,
            "avg_tone": round(avg_tone, 2) if avg_tone is not None else None,
            "tension": tension,
            "headlines": headlines,
        }
    except Exception as exc:  # pragma: no cover
        logger.warning("GDELT news failed for %r: %s", query, exc)
        return None
