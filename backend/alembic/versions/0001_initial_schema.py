"""initial schema

Revision ID: 0001_initial
Revises:
Create Date: 2026-01-01 00:00:00
"""
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op

revision: str = "0001_initial"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def _ts(table: sa.Table) -> None:  # pragma: no cover - helper unused at runtime
    pass


def upgrade() -> None:
    op.create_table(
        "assets",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("ticker", sa.String(20), nullable=False, unique=True),
        sa.Column("name", sa.String(255)),
        sa.Column("sector", sa.String(120)),
        sa.Column("industry", sa.String(120)),
        sa.Column("country", sa.String(80)),
        sa.Column("currency", sa.String(10)),
        sa.Column("market_cap", sa.Float),
        sa.Column("last_price", sa.Float),
        sa.Column("conviction_score", sa.Float),
        sa.Column("summary", sa.Text),
        sa.Column("data_source", sa.String(20), server_default="simulated"),
        sa.Column("data_provider", sa.String(40)),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_assets_ticker", "assets", ["ticker"])
    op.create_index("ix_assets_sector", "assets", ["sector"])
    op.create_index("ix_assets_conviction_score", "assets", ["conviction_score"])

    op.create_table(
        "market_scans",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("label", sa.String(255)),
        sa.Column("status", sa.String(20), server_default="pending"),
        sa.Column("requested_tickers", sa.JSON),
        sa.Column("num_assets", sa.Integer, server_default="0"),
        sa.Column("avg_score", sa.Float),
        sa.Column("error", sa.String(1000)),
        sa.Column("started_at", sa.DateTime(timezone=True)),
        sa.Column("finished_at", sa.DateTime(timezone=True)),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_market_scans_status", "market_scans", ["status"])

    op.create_table(
        "investment_theses",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("asset_id", sa.Integer, sa.ForeignKey("assets.id", ondelete="CASCADE")),
        sa.Column("scan_id", sa.Integer, sa.ForeignKey("market_scans.id", ondelete="SET NULL")),
        sa.Column("ticker", sa.String(20)),
        sa.Column("name", sa.String(255)),
        sa.Column("financial_score", sa.Float, server_default="0"),
        sa.Column("business_score", sa.Float, server_default="0"),
        sa.Column("future_score", sa.Float, server_default="0"),
        sa.Column("contrarian_score", sa.Float, server_default="0"),
        sa.Column("geopolitical_score", sa.Float, server_default="50"),
        sa.Column("conviction_score", sa.Float, server_default="0"),
        sa.Column("recommendation", sa.String(30), server_default="HOLD"),
        sa.Column("geo_risk", sa.String(20)),
        sa.Column("outlook", sa.String(20)),
        sa.Column("volatility", sa.String(20)),
        sa.Column("summary", sa.Text),
        sa.Column("thesis", sa.Text),
        sa.Column("risks", sa.Text),
        sa.Column("catalysts", sa.Text),
        sa.Column("engine", sa.String(20), server_default="heuristic"),
        sa.Column("data_source", sa.String(20), server_default="simulated"),
        sa.Column("data_provider", sa.String(40)),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_investment_theses_ticker", "investment_theses", ["ticker"])
    op.create_index("ix_investment_theses_asset_id", "investment_theses", ["asset_id"])
    op.create_index("ix_investment_theses_scan_id", "investment_theses", ["scan_id"])
    op.create_index("ix_investment_theses_conviction_score", "investment_theses", ["conviction_score"])

    op.create_table(
        "agent_reports",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("asset_id", sa.Integer, sa.ForeignKey("assets.id", ondelete="CASCADE")),
        sa.Column("thesis_id", sa.Integer, sa.ForeignKey("investment_theses.id", ondelete="CASCADE")),
        sa.Column("ticker", sa.String(20)),
        sa.Column("agent_role", sa.String(60)),
        sa.Column("score", sa.Float),
        sa.Column("summary", sa.Text),
        sa.Column("payload", sa.JSON),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_agent_reports_ticker", "agent_reports", ["ticker"])
    op.create_index("ix_agent_reports_agent_role", "agent_reports", ["agent_role"])

    op.create_table(
        "watchlists",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("ticker", sa.String(20), nullable=False, unique=True),
        sa.Column("name", sa.String(255)),
        sa.Column("conviction_score", sa.Float),
        sa.Column("note", sa.Text),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_watchlists_ticker", "watchlists", ["ticker"])

    op.create_table(
        "system_logs",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("level", sa.String(20), server_default="INFO"),
        sa.Column("source", sa.String(120)),
        sa.Column("message", sa.Text),
        sa.Column("context", sa.JSON),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_system_logs_level", "system_logs", ["level"])

    op.create_table(
        "historical_scores",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("asset_id", sa.Integer, sa.ForeignKey("assets.id", ondelete="CASCADE")),
        sa.Column("ticker", sa.String(20)),
        sa.Column("conviction_score", sa.Float),
        sa.Column("financial_score", sa.Float),
        sa.Column("business_score", sa.Float),
        sa.Column("future_score", sa.Float),
        sa.Column("contrarian_score", sa.Float),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_index("ix_historical_scores_ticker", "historical_scores", ["ticker"])
    op.create_index("ix_historical_scores_asset_id", "historical_scores", ["asset_id"])


def downgrade() -> None:
    op.drop_table("historical_scores")
    op.drop_table("system_logs")
    op.drop_table("watchlists")
    op.drop_table("agent_reports")
    op.drop_table("investment_theses")
    op.drop_table("market_scans")
    op.drop_table("assets")
