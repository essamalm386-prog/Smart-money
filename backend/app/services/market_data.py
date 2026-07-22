"""Market data service — REAL fundamentals & prices, with honest source tracking.

Provider priority (first that succeeds wins):
  1. yfinance      — real Yahoo Finance data, free, no key.
  2. Finnhub       — real data, requires a free FINNHUB_API_KEY (more reliable
                     than Yahoo from cloud hosts).
  3. synthetic     — deterministic SIMULATED data, clearly flagged, so the app
                     still runs offline / when every real source is unreachable.

Every ``Fundamentals`` carries ``source`` (yfinance|finnhub|synthetic),
``provider`` (human label) and ``is_real`` so the UI can display an honest
"Données réelles / simulées" badge.
"""
from __future__ import annotations

import hashlib
from dataclasses import dataclass, field
from typing import Optional

from app.config import settings
from app.logging_config import get_logger

logger = get_logger("smartmoney.market_data")

try:  # pragma: no cover - import guard
    import yfinance as yf  # type: ignore
    _YF_AVAILABLE = True
except Exception:  # pragma: no cover
    yf = None  # type: ignore
    _YF_AVAILABLE = False

try:  # pragma: no cover - import guard
    import requests  # type: ignore
    _REQUESTS_AVAILABLE = True
except Exception:  # pragma: no cover
    requests = None  # type: ignore
    _REQUESTS_AVAILABLE = False


_PROVIDER_LABELS = {
    "yfinance": "Yahoo Finance",
    "finnhub": "Finnhub",
    "synthetic": "Simulation",
}


@dataclass(slots=True)
class Fundamentals:
    ticker: str
    name: Optional[str] = None
    sector: Optional[str] = None
    industry: Optional[str] = None
    country: Optional[str] = None
    currency: Optional[str] = None
    market_cap: Optional[float] = None
    last_price: Optional[float] = None
    pe_ratio: Optional[float] = None
    forward_pe: Optional[float] = None
    peg_ratio: Optional[float] = None
    price_to_book: Optional[float] = None
    revenue_growth: Optional[float] = None
    earnings_growth: Optional[float] = None
    gross_margins: Optional[float] = None
    operating_margins: Optional[float] = None
    profit_margins: Optional[float] = None
    free_cashflow: Optional[float] = None
    total_debt: Optional[float] = None
    total_cash: Optional[float] = None
    debt_to_equity: Optional[float] = None
    return_on_equity: Optional[float] = None
    beta: Optional[float] = None
    recommendation_mean: Optional[float] = None
    number_of_analysts: Optional[int] = None
    # Momentum / price context
    price_52w_high: Optional[float] = None
    price_52w_low: Optional[float] = None
    pct_off_52w_high: Optional[float] = None  # 0..1, distance below the high
    return_6m: Optional[float] = None
    source: str = "yfinance"
    extra: dict = field(default_factory=dict)

    @property
    def provider(self) -> str:
        return _PROVIDER_LABELS.get(self.source, self.source)

    @property
    def is_real(self) -> bool:
        return self.source in ("yfinance", "finnhub")

    @property
    def data_source(self) -> str:
        return "real" if self.is_real else "simulated"


# --------------------------------------------------------------------------- #
# Synthetic (SIMULATED) fallback
# --------------------------------------------------------------------------- #
def _seed(ticker: str) -> float:
    digest = hashlib.sha256(ticker.encode("utf-8")).hexdigest()
    return int(digest[:8], 16) / 0xFFFFFFFF


def _synthetic(ticker: str) -> Fundamentals:
    r = _seed(ticker)
    r2 = _seed(ticker[::-1])
    price = round(20 + r * 480, 2)
    high = round(price * (1 + r2 * 0.4), 2)
    low = round(price * (0.6 + r * 0.2), 2)
    return Fundamentals(
        ticker=ticker,
        name=f"{ticker} Corp.",
        sector=["Technology", "Healthcare", "Consumer", "Industrials", "Energy"][int(r * 5) % 5],
        industry="Diversified",
        country="United States",
        currency="USD",
        market_cap=round(5.0e8 + r * 2.5e12, 2),
        last_price=price,
        pe_ratio=round(8 + r * 45, 2),
        forward_pe=round(7 + r2 * 40, 2),
        peg_ratio=round(0.5 + r2 * 2.5, 2),
        price_to_book=round(1 + r * 12, 2),
        revenue_growth=round(-0.05 + r * 0.45, 4),
        earnings_growth=round(-0.10 + r2 * 0.60, 4),
        gross_margins=round(0.2 + r * 0.6, 4),
        operating_margins=round(0.05 + r2 * 0.35, 4),
        profit_margins=round(0.02 + r * 0.30, 4),
        free_cashflow=round(-1e8 + r2 * 5e10, 2),
        total_debt=round(r * 4e10, 2),
        total_cash=round(r2 * 3e10, 2),
        debt_to_equity=round(r * 180, 2),
        return_on_equity=round(-0.05 + r2 * 0.45, 4),
        beta=round(0.5 + r * 1.6, 2),
        recommendation_mean=round(1.5 + r * 3.0, 2),
        number_of_analysts=int(3 + r2 * 40),
        price_52w_high=high,
        price_52w_low=low,
        pct_off_52w_high=round(max(0.0, (high - price) / high), 4) if high else None,
        return_6m=round(-0.2 + r2 * 0.6, 4),
        source="synthetic",
    )


# --------------------------------------------------------------------------- #
# yfinance (REAL)
# --------------------------------------------------------------------------- #
def _momentum_from_history(tk) -> dict:  # pragma: no cover - network path
    out: dict = {}
    try:
        hist = tk.history(period="1y", interval="1d")
        if hist is not None and not hist.empty:
            closes = hist["Close"].dropna()
            if len(closes) > 5:
                last = float(closes.iloc[-1])
                out["price_52w_high"] = round(float(closes.max()), 2)
                out["price_52w_low"] = round(float(closes.min()), 2)
                if out["price_52w_high"]:
                    out["pct_off_52w_high"] = round(
                        max(0.0, (out["price_52w_high"] - last) / out["price_52w_high"]), 4
                    )
                idx6 = max(0, len(closes) - 126)  # ~6 trading months
                ref = float(closes.iloc[idx6])
                if ref:
                    out["return_6m"] = round((last - ref) / ref, 4)
    except Exception as exc:
        logger.debug("history/momentum failed: %s", exc)
    return out


def _from_yfinance(ticker: str) -> Optional[Fundamentals]:  # pragma: no cover - network path
    if not _YF_AVAILABLE:
        return None
    try:
        tk = yf.Ticker(ticker)  # type: ignore[union-attr]
        info = tk.get_info()
        if not info or not (info.get("marketCap") or info.get("currentPrice")):
            return None
        g = info.get
        f = Fundamentals(
            ticker=ticker,
            name=g("longName") or g("shortName"),
            sector=g("sector"),
            industry=g("industry"),
            country=g("country"),
            currency=g("currency"),
            market_cap=g("marketCap"),
            last_price=g("currentPrice") or g("regularMarketPrice") or g("previousClose"),
            pe_ratio=g("trailingPE"),
            forward_pe=g("forwardPE"),
            peg_ratio=g("pegRatio") or g("trailingPegRatio"),
            price_to_book=g("priceToBook"),
            revenue_growth=g("revenueGrowth"),
            earnings_growth=g("earningsGrowth"),
            gross_margins=g("grossMargins"),
            operating_margins=g("operatingMargins"),
            profit_margins=g("profitMargins"),
            free_cashflow=g("freeCashflow"),
            total_debt=g("totalDebt"),
            total_cash=g("totalCash"),
            debt_to_equity=g("debtToEquity"),
            return_on_equity=g("returnOnEquity"),
            beta=g("beta"),
            recommendation_mean=g("recommendationMean"),
            number_of_analysts=g("numberOfAnalystOpinions"),
            price_52w_high=g("fiftyTwoWeekHigh"),
            price_52w_low=g("fiftyTwoWeekLow"),
            source="yfinance",
        )
        for k, v in _momentum_from_history(tk).items():
            setattr(f, k, v)
        if f.pct_off_52w_high is None and f.price_52w_high and f.last_price:
            f.pct_off_52w_high = round(max(0.0, (f.price_52w_high - f.last_price) / f.price_52w_high), 4)
        return f
    except Exception as exc:
        logger.warning("yfinance failed for %s: %s", ticker, exc)
        return None


# --------------------------------------------------------------------------- #
# Finnhub (REAL, optional key)
# --------------------------------------------------------------------------- #
def _from_finnhub(ticker: str) -> Optional[Fundamentals]:  # pragma: no cover - network path
    key = settings.finnhub_api_key
    if not key or not _REQUESTS_AVAILABLE:
        return None
    try:
        base = "https://finnhub.io/api/v1"
        prof = requests.get(f"{base}/stock/profile2", params={"symbol": ticker, "token": key}, timeout=15).json()
        metric = requests.get(f"{base}/stock/metric", params={"symbol": ticker, "metric": "all", "token": key}, timeout=15).json().get("metric", {})
        quote = requests.get(f"{base}/quote", params={"symbol": ticker, "token": key}, timeout=15).json()
        if not prof and not metric:
            return None
        m = metric.get
        price = quote.get("c")
        high = m("52WeekHigh")
        return Fundamentals(
            ticker=ticker,
            name=prof.get("name"),
            sector=prof.get("finnhubIndustry"),
            industry=prof.get("finnhubIndustry"),
            country=prof.get("country"),
            currency=prof.get("currency"),
            market_cap=(prof.get("marketCapitalization") or 0) * 1e6 or None,
            last_price=price,
            pe_ratio=m("peTTM"),
            peg_ratio=m("pegTTM"),
            price_to_book=m("pbQuarterly"),
            revenue_growth=(m("revenueGrowthTTMYoy") or 0) / 100 if m("revenueGrowthTTMYoy") else None,
            gross_margins=(m("grossMarginTTM") or 0) / 100 if m("grossMarginTTM") else None,
            operating_margins=(m("operatingMarginTTM") or 0) / 100 if m("operatingMarginTTM") else None,
            profit_margins=(m("netProfitMarginTTM") or 0) / 100 if m("netProfitMarginTTM") else None,
            debt_to_equity=m("totalDebt/totalEquityQuarterly"),
            return_on_equity=(m("roeTTM") or 0) / 100 if m("roeTTM") else None,
            beta=m("beta"),
            price_52w_high=high,
            price_52w_low=m("52WeekLow"),
            pct_off_52w_high=round(max(0.0, (high - price) / high), 4) if (high and price) else None,
            source="finnhub",
        )
    except Exception as exc:
        logger.warning("Finnhub failed for %s: %s", ticker, exc)
        return None


def get_fundamentals(ticker: str, allow_network: bool = True) -> Fundamentals:
    """Fetch REAL fundamentals, falling back through providers to synthetic."""
    ticker = ticker.strip().upper()
    if allow_network:
        for provider in (_from_yfinance, _from_finnhub):
            result = provider(ticker)
            if result is not None:
                logger.info("Real data for %s via %s", ticker, result.provider)
                return result
        logger.warning("No real source for %s; using SIMULATED data", ticker)
    return _synthetic(ticker)
