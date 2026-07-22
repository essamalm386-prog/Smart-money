"""Application configuration using Pydantic v2 settings."""
from __future__ import annotations

from functools import lru_cache
from typing import List

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Central, typed application settings loaded from environment / .env."""

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )

    # --- App ---
    app_name: str = "Smart Money"
    app_version: str = "1.0.0"
    environment: str = Field(default="development")
    debug: bool = True
    api_prefix: str = "/api"

    # --- Database ---
    database_url: str = "sqlite:///./smart_money.db"

    # --- LLM / CrewAI ---
    anthropic_api_key: str | None = Field(default=None, alias="ANTHROPIC_API_KEY")
    llm_model: str = "claude-3-5-sonnet-20241022"
    use_llm: bool = True  # if False -> always use deterministic heuristic engine

    # --- Real market-data providers ---
    # Optional free key from https://finnhub.io — more reliable than Yahoo from
    # cloud hosts. yfinance (no key) is tried first regardless.
    finnhub_api_key: str | None = Field(default=None, alias="FINNHUB_API_KEY")

    # Pappers — French company legal & financial registry (SIREN, comptes,
    # dirigeants). Free tier available. https://www.pappers.fr/api
    pappers_api_key: str | None = Field(default=None, alias="PAPPERS_API_KEY")
    pappers_base_url: str = "https://api.pappers.fr/v2"

    # --- Scanning defaults ---
    # Allow scans to reach external data sources (yfinance, GDELT, DuckDuckGo,
    # Pappers). Set ENABLE_NETWORK=false for fully offline / deterministic runs.
    enable_network: bool = Field(default=True, alias="ENABLE_NETWORK")
    default_universe: List[str] = Field(
        default_factory=lambda: [
            "AAPL", "MSFT", "NVDA", "GOOGL", "AMZN",
            "META", "TSLA", "AMD", "CRM", "SHOP",
        ]
    )
    min_market_cap: float = 5.0e8  # 500M USD floor
    max_pe: float = 120.0
    excluded_sectors: List[str] = Field(default_factory=list)

    # --- CORS ---
    cors_origins: List[str] = Field(default_factory=lambda: ["*"])

    # --- Logging ---
    log_level: str = "INFO"

    @field_validator("default_universe", "excluded_sectors", "cors_origins", mode="before")
    @classmethod
    def _split_csv(cls, value):
        if isinstance(value, str):
            return [v.strip() for v in value.split(",") if v.strip()]
        return value

    @property
    def anthropic_configured(self) -> bool:
        return bool(self.anthropic_api_key and self.anthropic_api_key.strip())

    @property
    def pappers_configured(self) -> bool:
        return bool(self.pappers_api_key and self.pappers_api_key.strip())

    @property
    def llm_enabled(self) -> bool:
        """LLM path only runs if explicitly enabled AND a key is present."""
        return self.use_llm and self.anthropic_configured


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()
