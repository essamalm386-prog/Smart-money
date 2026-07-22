"""Contrarian search service — media/sentiment signal via DuckDuckGo.

Used by the Contrarian analyst to gauge *media saturation*. The core thesis of
Smart Money is to find opportunities BEFORE they are widely covered, so a HIGH
volume of recent coverage lowers the contrarian score, and quiet-but-quality
names score higher.

Network + the search library are optional: a deterministic synthetic signal is
returned when unavailable so scans always complete.
"""
from __future__ import annotations

import hashlib
from dataclasses import dataclass, field
from typing import List

from app.logging_config import get_logger

logger = get_logger("smartmoney.search")

try:  # pragma: no cover - import guard
    from ddgs import DDGS  # type: ignore
    _DDG_AVAILABLE = True
except Exception:  # pragma: no cover
    try:
        from duckduckgo_search import DDGS  # type: ignore
        _DDG_AVAILABLE = True
    except Exception:
        DDGS = None  # type: ignore
        _DDG_AVAILABLE = False


@dataclass(slots=True)
class MediaSignal:
    ticker: str
    article_count: int
    saturation: float  # 0..1, 1 = heavily covered / crowded
    headlines: List[str] = field(default_factory=list)
    source: str = "duckduckgo"


def _seed(ticker: str) -> float:
    digest = hashlib.sha256(("media" + ticker).encode()).hexdigest()
    return int(digest[:8], 16) / 0xFFFFFFFF


def _synthetic(ticker: str) -> MediaSignal:
    r = _seed(ticker)
    count = int(r * 60)
    return MediaSignal(
        ticker=ticker,
        article_count=count,
        saturation=round(min(1.0, count / 50.0), 3),
        headlines=[f"{ticker}: analysts weigh outlook", f"{ticker} in focus this quarter"],
        source="synthetic",
    )


def get_media_signal(ticker: str, name: str | None = None,
                     allow_network: bool = True, max_results: int = 25) -> MediaSignal:
    """Estimate recent media saturation for a ticker."""
    ticker = ticker.strip().upper()
    query = f"{name or ticker} stock news"
    if _DDG_AVAILABLE and allow_network:
        try:  # pragma: no cover - network path
            with DDGS() as ddgs:  # type: ignore[operator]
                results = list(ddgs.news(query, max_results=max_results))
            count = len(results)
            headlines = [r.get("title", "") for r in results[:5] if r.get("title")]
            return MediaSignal(
                ticker=ticker,
                article_count=count,
                saturation=round(min(1.0, count / float(max_results)), 3),
                headlines=headlines,
                source="duckduckgo",
            )
        except Exception as exc:  # pragma: no cover
            logger.warning("DDG news failed for %s: %s", ticker, exc)
    return _synthetic(ticker)
