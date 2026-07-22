"""SQLAlchemy models package. Importing registers all tables on Base.metadata."""
from app.models.asset import Asset
from app.models.market_scan import MarketScan
from app.models.investment_thesis import InvestmentThesis
from app.models.agent_report import AgentReport
from app.models.watchlist import Watchlist
from app.models.system_log import SystemLog
from app.models.historical_score import HistoricalScore

__all__ = [
    "Asset",
    "MarketScan",
    "InvestmentThesis",
    "AgentReport",
    "Watchlist",
    "SystemLog",
    "HistoricalScore",
]
