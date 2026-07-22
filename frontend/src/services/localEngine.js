/**
 * On-device engine — a full, backend-free implementation of the data surface
 * the app needs. Scans run synchronously in the browser via the scoring
 * pipeline and everything is persisted to localStorage (see localStore.js).
 */
import { analyzeAsset } from '../engine/pipeline.js';
import { qualityGate } from '../engine/scoring.js';
import {
  listUniverse,
  tickersForSector,
  tickersForDomain,
  sectorLabel,
  domainLabel,
} from '../engine/universe.js';
import store, { KEYS } from './localStore.js';

const APP_VERSION = '1.0.0-offline';

// Offline data is produced by the on-device engine — flag it honestly so the UI
// can show a "simulated data" badge everywhere.
const DATA_SOURCE = 'simulated';
const DATA_PROVIDER = 'Simulation';

/** Default universe seeded on first load / used when a scan omits tickers. */
export const SEED_UNIVERSE = [
  'AAPL', 'MSFT', 'NVDA', 'GOOGL', 'AMZN',
  'META', 'TSLA', 'AMD', 'CRM', 'SHOP',
  'NFLX', 'PLTR', 'UBER', 'COIN', 'SNOW',
];

const DEFAULT_CONFIG = {
  min_market_cap: 5.0e8,
  excluded_sectors: [],
  max_pe: 120.0,
  scan_universe: [...SEED_UNIVERSE],
  use_llm: false,
  anthropic_configured: false,
  llm_model: 'on-device-heuristic',
};

// --- helpers ---------------------------------------------------------------
function normaliseTickers(list) {
  const seen = new Set();
  const out = [];
  (list || []).forEach((t) => {
    const v = String(t || '').trim().toUpperCase();
    if (v && !seen.has(v)) {
      seen.add(v);
      out.push(v);
    }
  });
  return out;
}

/**
 * Stamp the simulated-data source on an offline object if it is missing, so the
 * UI badge shows honestly even for records persisted before these fields existed.
 */
function withSimSource(obj) {
  if (!obj || typeof obj !== 'object') return obj;
  if (obj.data_source) return obj;
  return { ...obj, data_source: DATA_SOURCE, data_provider: DATA_PROVIDER };
}

function byCreatedDesc(a, b) {
  const ta = new Date(a.created_at || 0).getTime();
  const tb = new Date(b.created_at || 0).getTime();
  if (tb !== ta) return tb - ta;
  return (b.id || 0) - (a.id || 0);
}

function log(level, source, message) {
  store.insert(KEYS.logs, {
    level,
    source,
    message,
    created_at: new Date().toISOString(),
  });
}

// --- config ----------------------------------------------------------------
export function getConfig() {
  const stored = store.read(KEYS.config, null) || {};
  return { ...DEFAULT_CONFIG, ...stored };
}

export function updateConfig(partial) {
  const merged = { ...getConfig(), ...(partial || {}) };
  // Never let the client flip the offline engine's static capability flags.
  merged.anthropic_configured = false;
  merged.llm_model = DEFAULT_CONFIG.llm_model;
  store.write(KEYS.config, merged);
  log('INFO', 'config', 'Configuration updated (offline).');
  return merged;
}

// --- universe --------------------------------------------------------------
/** Secteurs & thématiques investissables (parité avec GET /api/universe). */
export function getUniverse() {
  return listUniverse();
}

// --- scans -----------------------------------------------------------------
export function createScan({ tickers, label, sector, domain } = {}) {
  const config = getConfig();
  let requested = normaliseTickers(tickers);

  // Un scan par secteur / thématique étend automatiquement en liste de tickers.
  let resolvedLabel = label;
  // Libellé du secteur utilisé pour taguer les actifs (filtre secteur hors ligne).
  let sectorTag = null;
  if (requested.length === 0 && sector) {
    requested = normaliseTickers(tickersForSector(sector));
    sectorTag = sectorLabel(sector);
    if (!resolvedLabel) resolvedLabel = `Secteur : ${sectorTag}`;
  } else if (requested.length === 0 && domain) {
    requested = normaliseTickers(tickersForDomain(domain));
    if (!resolvedLabel) resolvedLabel = `Thématique : ${domainLabel(domain)}`;
  }

  if (requested.length === 0) {
    requested = normaliseTickers(config.scan_universe);
  }
  if (requested.length === 0) requested = [...SEED_UNIVERSE];

  const now = Date.now();
  const scanId = store.nextId(KEYS.scans);
  const startedAt = new Date(now).toISOString();

  const excluded = new Set(
    (config.excluded_sectors || []).map((s) => String(s).toLowerCase())
  );

  const producedTheses = [];
  let step = 0;

  requested.forEach((ticker) => {
    const result = analyzeAsset(ticker);
    const f = result.fundamentals;

    // Respect the quality gate + excluded sectors so Config actually matters.
    const passesGate = qualityGate(
      f.market_cap,
      f.pe_ratio,
      config.min_market_cap,
      config.max_pe
    );
    const sectorExcluded = excluded.has(String(f.sector || '').toLowerCase());
    if (!passesGate || sectorExcluded) {
      log('INFO', 'scan', `${ticker} filtered out (gate/sector).`);
      return;
    }

    step += 1;
    const ts = new Date(now + step).toISOString();

    // Upsert asset (keyed by ticker).
    const assets = store.getCollection(KEYS.assets);
    let asset = assets.find((a) => a.ticker === ticker);
    if (!asset) {
      asset = { id: store.nextId(KEYS.assets), ticker };
      assets.push(asset);
    }
    Object.assign(asset, {
      name: result.name,
      // Scan par secteur : on tague avec le libellé du secteur choisi pour que
      // le filtre par secteur du tableau de bord soit pertinent hors ligne.
      sector: sectorTag || f.sector,
      industry: f.industry,
      country: f.country,
      market_cap: f.market_cap,
      currency: f.currency,
      last_price: f.last_price,
      conviction_score: result.conviction_score,
      geopolitical_score: result.geopolitical_score,
      geo_risk: result.geo_risk,
      outlook: result.outlook,
      volatility: result.volatility,
      data_source: DATA_SOURCE,
      data_provider: DATA_PROVIDER,
      updated_at: ts,
    });
    store.setCollection(KEYS.assets, assets);

    // Thesis (includes detail narrative fields).
    const thesis = store.insert(KEYS.theses, {
      ticker,
      name: result.name,
      scan_id: scanId,
      asset_id: asset.id,
      sector: sectorTag || f.sector,
      financial_score: result.financial_score,
      business_score: result.business_score,
      future_score: result.future_score,
      contrarian_score: result.contrarian_score,
      geopolitical_score: result.geopolitical_score,
      geo_risk: result.geo_risk,
      outlook: result.outlook,
      volatility: result.volatility,
      conviction_score: result.conviction_score,
      recommendation: result.recommendation,
      summary: result.summary,
      engine: result.engine,
      thesis: result.thesis,
      risks: result.risks,
      catalysts: result.catalysts,
      data_source: DATA_SOURCE,
      data_provider: DATA_PROVIDER,
      created_at: ts,
    });
    producedTheses.push(thesis);

    // Agent reports (one per analyst).
    result.agent_outputs.forEach((out) => {
      store.insert(KEYS.reports, {
        ticker,
        asset_id: asset.id,
        thesis_id: thesis.id,
        agent_role: out.role,
        score: out.score,
        summary: out.summary,
        payload: out.payload,
        created_at: ts,
      });
    });

    // Historical score point.
    store.insert(KEYS.history, {
      ticker,
      asset_id: asset.id,
      conviction_score: result.conviction_score,
      created_at: ts,
    });
  });

  const numAssets = producedTheses.length;
  const avgScore =
    numAssets > 0
      ? Math.round(
          (producedTheses.reduce((s, t) => s + (t.conviction_score || 0), 0) /
            numAssets) *
            100
        ) / 100
      : null;

  const scan = {
    id: scanId,
    label: resolvedLabel || 'Scan manuel',
    status: 'completed',
    requested_tickers: requested,
    num_assets: numAssets,
    avg_score: avgScore,
    error: null,
    started_at: startedAt,
    finished_at: new Date(Date.now()).toISOString(),
    created_at: startedAt,
  };
  const scans = store.getCollection(KEYS.scans);
  scans.push(scan);
  store.setCollection(KEYS.scans, scans);

  log('INFO', 'scan', `Scan #${scanId} completed: ${numAssets} assets analyzed.`);
  return scan;
}

/** Seed an initial scan on first load so the dashboard is populated offline. */
export function ensureSeed() {
  const scans = store.getCollection(KEYS.scans);
  if (scans.length === 0) {
    createScan({ tickers: SEED_UNIVERSE, label: 'Initial offline scan' });
  }
}

// --- reads -----------------------------------------------------------------
export function health() {
  return { status: 'ok', time: new Date().toISOString(), version: APP_VERSION };
}

export function listAssets({ sector, limit = 200 } = {}) {
  let assets = store.getCollection(KEYS.assets).slice();
  if (sector) assets = assets.filter((a) => a.sector === sector);
  assets.sort((a, b) => (b.conviction_score || 0) - (a.conviction_score || 0));
  return assets.slice(0, limit).map(withSimSource);
}

export function getAsset(rawTicker) {
  const ticker = String(rawTicker || '').trim().toUpperCase();
  const asset = store.getCollection(KEYS.assets).find((a) => a.ticker === ticker);
  if (!asset) return null;

  const latest = store
    .getCollection(KEYS.theses)
    .filter((t) => t.ticker === ticker)
    .sort(byCreatedDesc)[0] || null;

  const reports = latest
    ? store
        .getCollection(KEYS.reports)
        .filter((r) => r.thesis_id === latest.id)
        .sort((a, b) => (a.id || 0) - (b.id || 0))
    : [];

  const score_history = store
    .getCollection(KEYS.history)
    .filter((h) => h.ticker === ticker)
    .sort(
      (a, b) => new Date(a.created_at || 0) - new Date(b.created_at || 0)
    )
    .map((h) => ({ conviction_score: h.conviction_score, created_at: h.created_at }));

  return {
    asset: withSimSource(asset),
    latest_thesis: withSimSource(latest),
    reports,
    score_history,
  };
}

export function listTheses(limit = 50, latestPerTicker = true) {
  let rows = store.getCollection(KEYS.theses).slice().sort(byCreatedDesc);
  if (latestPerTicker) {
    const seen = new Set();
    rows = rows.filter((t) => {
      if (seen.has(t.ticker)) return false;
      seen.add(t.ticker);
      return true;
    });
  }
  rows.sort((a, b) => (b.conviction_score || 0) - (a.conviction_score || 0));
  return rows.slice(0, limit).map(withSimSource);
}

export function getThesis(id) {
  const tid = Number(id);
  const thesis = store.getCollection(KEYS.theses).find((t) => t.id === tid);
  if (!thesis) return null;
  const agent_reports = store
    .getCollection(KEYS.reports)
    .filter((r) => r.thesis_id === tid)
    .sort((a, b) => (a.id || 0) - (b.id || 0));
  return { ...withSimSource(thesis), agent_reports };
}

export function listScans(limit = 50) {
  return store.getCollection(KEYS.scans).slice().sort(byCreatedDesc).slice(0, limit);
}

export function getScan(id) {
  const sid = Number(id);
  const scan = store.getCollection(KEYS.scans).find((s) => s.id === sid);
  if (!scan) return null;
  const theses = store
    .getCollection(KEYS.theses)
    .filter((t) => t.scan_id === sid)
    .sort((a, b) => (b.conviction_score || 0) - (a.conviction_score || 0))
    .map(withSimSource);
  return { scan, theses };
}

export function listWatchlist() {
  return store.getCollection(KEYS.watchlist).slice().sort(byCreatedDesc);
}

export function addWatchlist({ ticker, note } = {}) {
  const t = String(ticker || '').trim().toUpperCase();
  if (!t) throw new Error('Ticker is required');
  const list = store.getCollection(KEYS.watchlist);
  const existing = list.find((w) => w.ticker === t);
  if (existing) {
    if (note !== undefined && note !== null) existing.note = note;
    store.setCollection(KEYS.watchlist, list);
    return existing;
  }
  const asset = store.getCollection(KEYS.assets).find((a) => a.ticker === t);
  return store.insert(KEYS.watchlist, {
    ticker: t,
    name: asset ? asset.name : null,
    conviction_score: asset ? asset.conviction_score : null,
    note: note ?? null,
    created_at: new Date().toISOString(),
  });
}

export function removeWatchlist(rawTicker) {
  const t = String(rawTicker || '').trim().toUpperCase();
  const list = store.getCollection(KEYS.watchlist);
  const next = list.filter((w) => w.ticker !== t);
  store.setCollection(KEYS.watchlist, next);
  return { ok: true };
}

export function listReports({ agent_role, ticker, limit = 50 } = {}) {
  let rows = store.getCollection(KEYS.reports).slice();
  if (agent_role) rows = rows.filter((r) => r.agent_role === agent_role);
  if (ticker) {
    const t = String(ticker).trim().toUpperCase();
    rows = rows.filter((r) => r.ticker === t);
  }
  rows.sort(byCreatedDesc);
  return rows.slice(0, limit);
}

export function listLogs({ level, limit = 100 } = {}) {
  let rows = store.getCollection(KEYS.logs).slice();
  if (level) rows = rows.filter((r) => r.level === String(level).toUpperCase());
  rows.sort(byCreatedDesc);
  return rows.slice(0, limit);
}

// --- Pappers (entreprises françaises) --------------------------------------
// Le registre légal Pappers exige le serveur + une clé API. Hors ligne, on ne
// peut RIEN renvoyer d'honnête : on n'invente jamais de fausses entreprises.
const PAPPERS_OFFLINE_REASON =
  "Disponible uniquement en mode serveur (connectez l'app à votre serveur en ligne).";

export function pappersStatus() {
  return { provider: 'Pappers', configured: false };
}

export function pappersSearch() {
  return { available: false, provider: 'Pappers', reason: PAPPERS_OFFLINE_REASON };
}

export function pappersCompany() {
  return { available: false, provider: 'Pappers', reason: PAPPERS_OFFLINE_REASON };
}

export default {
  SEED_UNIVERSE,
  ensureSeed,
  getUniverse,
  health,
  listAssets,
  getAsset,
  listTheses,
  getThesis,
  createScan,
  listScans,
  getScan,
  listWatchlist,
  addWatchlist,
  removeWatchlist,
  listReports,
  getConfig,
  updateConfig,
  listLogs,
  pappersStatus,
  pappersSearch,
  pappersCompany,
};
