"""Investable universe — curated tickers grouped by sector and by theme/domain.

Powers "discover where to invest" screening: the user picks a sector (e.g.
Technologie) or an investment theme (e.g. Intelligence Artificielle) and the
scan expands it into a concrete list of tickers to analyse. Labels are in
French for the UI.
"""
from __future__ import annotations

from typing import Dict, List

# --- Sectors (GICS-style), French labels --------------------------------- #
SECTORS: Dict[str, Dict] = {
    "technologie": {
        "label": "Technologie",
        "tickers": ["AAPL", "MSFT", "NVDA", "AVGO", "ORCL", "CRM", "ADBE", "AMD", "CSCO", "TXN"],
    },
    "services_communication": {
        "label": "Services de communication",
        "tickers": ["GOOGL", "META", "NFLX", "DIS", "TMUS", "T", "VZ", "SPOT"],
    },
    "consommation_discretionnaire": {
        "label": "Consommation discrétionnaire",
        "tickers": ["AMZN", "TSLA", "HD", "NKE", "MCD", "SBUX", "LOW", "BKNG"],
    },
    "consommation_base": {
        "label": "Consommation de base",
        "tickers": ["PG", "KO", "PEP", "COST", "WMT", "MDLZ", "CL", "MO"],
    },
    "sante": {
        "label": "Santé",
        "tickers": ["UNH", "JNJ", "LLY", "MRK", "ABBV", "PFE", "TMO", "AMGN", "ISRG"],
    },
    "finance": {
        "label": "Finance",
        "tickers": ["JPM", "BAC", "WFC", "GS", "MS", "BLK", "SCHW", "V", "MA", "AXP"],
    },
    "industrie": {
        "label": "Industrie",
        "tickers": ["CAT", "BA", "HON", "GE", "UPS", "RTX", "DE", "LMT", "UNP"],
    },
    "energie": {
        "label": "Énergie",
        "tickers": ["XOM", "CVX", "COP", "SLB", "EOG", "PSX", "MPC", "OXY"],
    },
    "materiaux": {
        "label": "Matériaux",
        "tickers": ["LIN", "SHW", "FCX", "NEM", "APD", "ECL", "DOW"],
    },
    "immobilier": {
        "label": "Immobilier",
        "tickers": ["PLD", "AMT", "EQIX", "SPG", "O", "CCI", "PSA"],
    },
    "services_publics": {
        "label": "Services publics",
        "tickers": ["NEE", "DUK", "SO", "D", "AEP", "EXC", "SRE"],
    },
}

# --- Investment themes / domains, French labels -------------------------- #
DOMAINS: Dict[str, Dict] = {
    "ia": {
        "label": "Intelligence Artificielle",
        "tickers": ["NVDA", "MSFT", "GOOGL", "META", "AMD", "PLTR", "SNOW", "AI", "SMCI"],
    },
    "semi_conducteurs": {
        "label": "Semi-conducteurs",
        "tickers": ["NVDA", "AMD", "AVGO", "TSM", "ASML", "QCOM", "MU", "INTC", "TXN", "SMCI"],
    },
    "cloud_logiciel": {
        "label": "Cloud & Logiciel",
        "tickers": ["MSFT", "CRM", "ADBE", "ORCL", "NOW", "SNOW", "DDOG", "NET", "WDAY"],
    },
    "cybersecurite": {
        "label": "Cybersécurité",
        "tickers": ["PANW", "CRWD", "ZS", "FTNT", "S", "OKTA", "NET", "CYBR"],
    },
    "fintech": {
        "label": "Fintech & Paiements",
        "tickers": ["V", "MA", "PYPL", "SQ", "COIN", "SOFI", "AXP", "FI", "GPN"],
    },
    "energie_propre": {
        "label": "Énergie propre",
        "tickers": ["ENPH", "FSLR", "SEDG", "NEE", "PLUG", "RUN", "BE", "ICLN"],
    },
    "sante_biotech": {
        "label": "Santé & Biotech",
        "tickers": ["LLY", "NVO", "MRNA", "VRTX", "REGN", "AMGN", "ISRG", "BIIB"],
    },
    "vehicules_electriques": {
        "label": "Véhicules électriques & Mobilité",
        "tickers": ["TSLA", "RIVN", "LCID", "F", "GM", "NIO", "BYDDY", "CHPT"],
    },
    "ecommerce": {
        "label": "E-commerce",
        "tickers": ["AMZN", "SHOP", "MELI", "SE", "ETSY", "BABA", "PDD", "EBAY"],
    },
    "defense_aerospatiale": {
        "label": "Défense & Aérospatiale",
        "tickers": ["LMT", "RTX", "NOC", "GD", "BA", "LHX", "HWM"],
    },
    "luxe_marques": {
        "label": "Luxe & Grandes marques",
        "tickers": ["LVMUY", "NKE", "SBUX", "MC.PA", "RMS.PA", "EL", "TPR"],
    },
}


def list_universe() -> Dict[str, List[Dict]]:
    """Return sectors and domains with labels and ticker counts for the UI."""
    return {
        "sectors": [
            {"key": k, "label": v["label"], "count": len(v["tickers"]), "tickers": v["tickers"]}
            for k, v in SECTORS.items()
        ],
        "domains": [
            {"key": k, "label": v["label"], "count": len(v["tickers"]), "tickers": v["tickers"]}
            for k, v in DOMAINS.items()
        ],
    }


def tickers_for_sector(key: str) -> List[str]:
    entry = SECTORS.get(key.lower().strip())
    return list(entry["tickers"]) if entry else []


def tickers_for_domain(key: str) -> List[str]:
    entry = DOMAINS.get(key.lower().strip())
    return list(entry["tickers"]) if entry else []


def sector_label(key: str) -> str:
    entry = SECTORS.get((key or "").lower().strip())
    return entry["label"] if entry else key
