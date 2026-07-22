"""Pappers integration — French company legal & financial registry.

Pappers (https://www.pappers.fr/api) exposes legal and accounting data on French
companies: SIREN/SIRET, legal form, executives (dirigeants), share capital,
headcount, and annual accounts (chiffre d'affaires, résultat…). This complements
the stock-market data (Yahoo/Finnhub) with company-registry information.

API v2:
  - GET {base}/recherche?q=...&api_token=KEY        (search)
  - GET {base}/entreprise?siren=...&api_token=KEY   (company detail)

HONESTY: unlike market data, this is an official registry — we NEVER fabricate
company data. When no API key is configured or the call fails, we return
``available=False`` with a clear reason, so the UI shows "source non configurée"
rather than fake companies.
"""
from __future__ import annotations

from typing import Any, Dict, List, Optional

from app.config import settings
from app.logging_config import get_logger

logger = get_logger("smartmoney.pappers")

try:  # pragma: no cover - import guard
    import requests  # type: ignore
    _REQUESTS_AVAILABLE = True
except Exception:  # pragma: no cover
    requests = None  # type: ignore
    _REQUESTS_AVAILABLE = False


def _unavailable(reason: str) -> Dict[str, Any]:
    return {"available": False, "reason": reason, "provider": "Pappers"}


def _get(path: str, params: Dict[str, Any]) -> Optional[dict]:  # pragma: no cover - network
    url = f"{settings.pappers_base_url}{path}"
    params = {**params, "api_token": settings.pappers_api_key}
    resp = requests.get(url, params=params, timeout=20)
    if resp.status_code == 401:
        raise PermissionError("Clé Pappers invalide (401).")
    if resp.status_code == 429:
        raise RuntimeError("Quota Pappers atteint (429).")
    resp.raise_for_status()
    return resp.json()


def _pick(d: dict, *keys, default=None):
    for k in keys:
        v = d.get(k)
        if v not in (None, "", []):
            return v
    return default


def _normalise_search_item(r: dict) -> Dict[str, Any]:
    siege = r.get("siege") or {}
    return {
        "siren": _pick(r, "siren"),
        "nom": _pick(r, "nom_entreprise", "denomination", "nom", default="—"),
        "forme_juridique": _pick(r, "forme_juridique"),
        "code_naf": _pick(r, "code_naf", "code_ape"),
        "libelle_naf": _pick(r, "libelle_code_naf", "libelle_code_ape"),
        "ville": _pick(siege, "ville"),
        "code_postal": _pick(siege, "code_postal"),
        "date_creation": _pick(r, "date_creation"),
    }


def _normalise_finances(finances: List[dict]) -> List[Dict[str, Any]]:
    out = []
    for f in finances or []:
        out.append({
            "annee": _pick(f, "annee", "year"),
            "chiffre_affaires": _pick(f, "chiffre_affaires", "ca"),
            "resultat": _pick(f, "resultat", "resultat_net"),
            "marge_brute": _pick(f, "marge_brute"),
            "effectif": _pick(f, "effectif"),
        })
    # Most recent first.
    out.sort(key=lambda x: (x["annee"] or 0), reverse=True)
    return out


def _normalise_company(r: dict) -> Dict[str, Any]:
    siege = r.get("siege") or {}
    dirigeants = []
    for d in (r.get("dirigeants") or [])[:10]:
        nom = " ".join(x for x in [_pick(d, "prenom"), _pick(d, "nom")] if x) or _pick(d, "nom_complet", "denomination")
        dirigeants.append({"nom": nom, "qualite": _pick(d, "qualite", "fonction")})
    return {
        "siren": _pick(r, "siren"),
        "nom": _pick(r, "nom_entreprise", "denomination", default="—"),
        "forme_juridique": _pick(r, "forme_juridique"),
        "capital": _pick(r, "capital"),
        "date_creation": _pick(r, "date_creation"),
        "code_naf": _pick(r, "code_naf", "code_ape"),
        "libelle_naf": _pick(r, "libelle_code_naf", "libelle_code_ape"),
        "effectif": _pick(r, "effectif"),
        "adresse": " ".join(x for x in [
            _pick(siege, "adresse_ligne_1", "adresse"),
            _pick(siege, "code_postal"),
            _pick(siege, "ville"),
        ] if x),
        "dirigeants": dirigeants,
        "finances": _normalise_finances(r.get("finances") or []),
        "site_web": _pick(r, "site_web"),
    }


def status() -> Dict[str, Any]:
    return {"provider": "Pappers", "configured": settings.pappers_configured}


def search_companies(query: str, allow_network: bool = True, per_page: int = 15) -> Dict[str, Any]:
    """Search French companies by name / SIREN via Pappers."""
    query = (query or "").strip()
    if not query:
        return {"available": True, "provider": "Pappers", "query": query, "results": []}
    if not settings.pappers_configured:
        return _unavailable("Clé API Pappers non configurée (PAPPERS_API_KEY).")
    if not _REQUESTS_AVAILABLE or not allow_network:
        return _unavailable("Accès réseau indisponible.")
    try:  # pragma: no cover - network path
        data = _get("/recherche", {"q": query, "par_page": per_page}) or {}
        results = data.get("resultats") or data.get("results") or []
        return {
            "available": True,
            "provider": "Pappers",
            "query": query,
            "total": data.get("total"),
            "results": [_normalise_search_item(r) for r in results],
        }
    except Exception as exc:  # pragma: no cover
        logger.warning("Pappers search failed: %s", exc)
        return _unavailable(f"Erreur Pappers : {exc}")


def get_company(siren: str, allow_network: bool = True) -> Dict[str, Any]:
    """Fetch a French company's registry + accounts by SIREN."""
    siren = (siren or "").strip().replace(" ", "")
    if not siren:
        return _unavailable("SIREN manquant.")
    if not settings.pappers_configured:
        return _unavailable("Clé API Pappers non configurée (PAPPERS_API_KEY).")
    if not _REQUESTS_AVAILABLE or not allow_network:
        return _unavailable("Accès réseau indisponible.")
    try:  # pragma: no cover - network path
        data = _get("/entreprise", {"siren": siren}) or {}
        if not data or not (data.get("siren") or data.get("nom_entreprise")):
            return _unavailable("Entreprise introuvable.")
        return {"available": True, "provider": "Pappers", "company": _normalise_company(data)}
    except Exception as exc:  # pragma: no cover
        logger.warning("Pappers company failed: %s", exc)
        return _unavailable(f"Erreur Pappers : {exc}")
