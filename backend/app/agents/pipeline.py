"""Analysis pipeline — orchestrates the four agents into one thesis per asset."""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import List

from app.agents import analyzers
from app.agents.analyzers import AgentOutput
from app.agents.crew import llm_narrative
from app.config import settings
from app.logging_config import get_logger
from app.services.geopolitics import analyze_geopolitics
from app.services.market_data import Fundamentals, get_fundamentals
from app.services.scoring import (
    SubScores,
    conviction_score,
    recommendation_for,
)
from app.services.search import get_media_signal

logger = get_logger("smartmoney.pipeline")


@dataclass(slots=True)
class AnalysisResult:
    ticker: str
    name: str
    fundamentals: Fundamentals
    financial_score: float
    business_score: float
    future_score: float
    contrarian_score: float
    geopolitical_score: float
    geo_risk: str
    conviction_score: float
    recommendation: str
    outlook: str
    volatility: str
    engine: str
    data_source: str
    data_provider: str
    summary: str
    thesis: str
    risks: str
    catalysts: str
    agent_outputs: List[AgentOutput] = field(default_factory=list)


def _fluctuation_forecast(future: float, contrarian: float, geo: float,
                          beta: float | None) -> tuple[str, str]:
    """Derive a directional outlook + expected volatility (French labels)."""
    # Directional bias: forward potential + re-rating room, tempered by geo risk.
    direction = 0.45 * future + 0.25 * contrarian + 0.30 * geo
    if direction >= 62:
        outlook = "Haussière"
    elif direction >= 45:
        outlook = "Neutre"
    else:
        outlook = "Prudente"
    # Expected volatility: high beta + low geo score + high contrarian surprise.
    b = beta if beta is not None else 1.0
    vol_index = (b - 1.0) * 40 + (100 - geo) * 0.5 + (100 - contrarian) * 0.2
    if vol_index >= 45:
        volatility = "Élevée"
    elif vol_index >= 20:
        volatility = "Modérée"
    else:
        volatility = "Faible"
    return outlook, volatility


def _heuristic_narrative(name: str, ticker: str, rec: str, conviction: float,
                         outputs: List[AgentOutput]) -> dict:
    top = max(outputs, key=lambda o: o.score)
    weak = min(outputs, key=lambda o: o.score)
    thesis = (
        f"{name} ({ticker}) scores {conviction:.0f}/100 conviction ({rec}). "
        f"Strongest dimension: {top.role} ({top.score:.0f}). "
        f"Weakest dimension: {weak.role} ({weak.score:.0f})."
    )
    risks = f"Primary risk stems from {weak.role.lower()} ({weak.summary})"
    catalysts = f"Upside catalyst from {top.role.lower()} ({top.summary})"
    return {"thesis": thesis, "risks": risks, "catalysts": catalysts}


def analyze_asset(ticker: str, allow_network: bool = True) -> AnalysisResult:
    """Run the full four-agent analysis for a single ticker."""
    ticker = ticker.strip().upper()
    logger.info("Analyzing %s", ticker)

    fundamentals = get_fundamentals(ticker, allow_network=allow_network)
    media = get_media_signal(ticker, fundamentals.name, allow_network=allow_network)

    geo_signal = analyze_geopolitics(
        ticker, fundamentals.country, fundamentals.sector,
        name=fundamentals.name, allow_network=allow_network,
    )

    quant = analyzers.quant_analysis(fundamentals)
    business = analyzers.business_analysis(fundamentals)
    future = analyzers.future_analysis(fundamentals)
    contrarian = analyzers.contrarian_analysis(fundamentals, media)
    geopolitical = analyzers.geopolitical_analysis(geo_signal)
    outputs = [quant, business, future, contrarian, geopolitical]

    sub = SubScores(
        financial=quant.score,
        business=business.score,
        future=future.score,
        contrarian=contrarian.score,
        geopolitics=geopolitical.score,
    )
    conviction = conviction_score(sub)
    rec = recommendation_for(conviction)
    outlook, volatility = _fluctuation_forecast(
        future.score, contrarian.score, geopolitical.score, fundamentals.beta
    )

    name = fundamentals.name or ticker
    engine = "heuristic"
    narrative = _heuristic_narrative(name, ticker, rec, conviction, outputs)

    # Optional LLM enrichment (safe no-op when unavailable).
    llm = llm_narrative(
        ticker, name, sub.as_dict(), conviction, rec,
        evidence={o.role: o.payload for o in outputs},
    )
    if llm and llm.get("thesis"):
        engine = "llm"
        narrative["thesis"] = llm["thesis"]
        narrative["risks"] = llm.get("risks") or narrative["risks"]
        narrative["catalysts"] = llm.get("catalysts") or narrative["catalysts"]

    summary = (
        f"{rec} — conviction {conviction:.0f}/100 "
        f"(fin {sub.financial:.0f} / biz {sub.business:.0f} / "
        f"fut {sub.future:.0f} / contra {sub.contrarian:.0f} / "
        f"géo {sub.geopolitics:.0f}) · prévision {outlook.lower()}, "
        f"volatilité {volatility.lower()}."
    )

    return AnalysisResult(
        ticker=ticker,
        name=name,
        fundamentals=fundamentals,
        financial_score=sub.financial,
        business_score=sub.business,
        future_score=sub.future,
        contrarian_score=sub.contrarian,
        geopolitical_score=sub.geopolitics,
        geo_risk=geo_signal.risk_level,
        conviction_score=conviction,
        recommendation=rec,
        outlook=outlook,
        volatility=volatility,
        engine=engine,
        data_source=fundamentals.data_source,
        data_provider=fundamentals.provider,
        summary=summary,
        thesis=narrative["thesis"],
        risks=narrative["risks"],
        catalysts=narrative["catalysts"],
        agent_outputs=outputs,
    )
