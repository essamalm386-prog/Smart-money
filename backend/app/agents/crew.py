"""CrewAI production crew definition.

When an Anthropic API key is configured AND ``use_llm`` is enabled, this module
builds a four-agent CrewAI crew (Quant, Business, Contrarian, Investment
Committee) that produces a narrative investment thesis. The numeric sub-scores
always come from the deterministic engine in ``analyzers.py``; the LLM layer is
responsible for synthesis, narrative, risks and catalysts only.

CrewAI and anthropic are imported lazily so the service runs perfectly without
them installed.
"""
from __future__ import annotations

from typing import Optional

from app.config import settings
from app.logging_config import get_logger

logger = get_logger("smartmoney.crew")


def crewai_available() -> bool:
    try:  # pragma: no cover - import guard
        import crewai  # noqa: F401
        return True
    except Exception:
        return False


def build_smart_money_crew():  # pragma: no cover - requires crewai + key
    """Construct the CrewAI crew. Raises if crewai is unavailable."""
    from crewai import Agent, Crew, Process

    try:
        from crewai import LLM
        llm = LLM(model=f"anthropic/{settings.llm_model}", api_key=settings.anthropic_api_key)
    except Exception:
        llm = None

    common = dict(llm=llm, allow_delegation=False, verbose=settings.debug)

    quant = Agent(
        role="Quant Analyst",
        goal="Assess valuation, growth, cash-flow, debt and margins with rigor.",
        backstory="A quantitative fundamental analyst who distrusts hype and loves clean balance sheets.",
        **common,
    )
    business = Agent(
        role="Business Analyst",
        goal="Judge the moat, addressable market, management quality and competitiveness.",
        backstory="A long-horizon business strategist focused on durable competitive advantage.",
        **common,
    )
    contrarian = Agent(
        role="Contrarian Analyst",
        goal="Detect media saturation and find quality the crowd has not noticed yet.",
        backstory="A contrarian who buys before the story hits the front page and sells into euphoria.",
        **common,
    )
    committee = Agent(
        role="Investment Committee",
        goal="Synthesise the analysts into a single conviction thesis, risks and catalysts.",
        backstory="A disciplined investment committee chair who produces the final verdict.",
        **common,
    )
    return {
        "agents": {"quant": quant, "business": business,
                   "contrarian": contrarian, "committee": committee},
        "Crew": Crew,
        "Process": Process,
    }


def llm_narrative(ticker: str, name: str, sub_scores: dict, conviction: float,
                  recommendation: str, evidence: dict) -> Optional[dict]:
    """Ask the committee (via CrewAI) for a narrative synthesis.

    Returns None if the LLM path is unavailable so the caller falls back to the
    deterministic narrative. Never raises.
    """
    if not settings.llm_enabled or not crewai_available():
        return None
    try:  # pragma: no cover - network / LLM path
        from crewai import Task

        crew_parts = build_smart_money_crew()
        committee = crew_parts["agents"]["committee"]
        Crew = crew_parts["Crew"]
        Process = crew_parts["Process"]

        task = Task(
            description=(
                f"Company {name} ({ticker}). Sub-scores: {sub_scores}. "
                f"Weighted conviction={conviction}, recommendation={recommendation}. "
                f"Evidence: {evidence}. Write a concise institutional thesis: 2-3 sentence "
                f"thesis, 3 key risks, 3 potential catalysts."
            ),
            expected_output="A JSON object with keys: thesis, risks, catalysts.",
            agent=committee,
        )
        crew = Crew(agents=[committee], tasks=[task], process=Process.sequential,
                    verbose=settings.debug)
        result = crew.kickoff()
        return {"thesis": str(result), "risks": None, "catalysts": None, "engine": "llm"}
    except Exception as exc:  # pragma: no cover
        logger.warning("LLM narrative failed for %s: %s", ticker, exc)
        return None
