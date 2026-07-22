/**
 * Unified data provider.
 *
 * Reads the active data-source mode from localStorage:
 *   - 'local'  (DEFAULT): fully offline, delegates to the on-device engine.
 *   - 'remote': delegates to the FastAPI backend via the shared axios instance.
 *
 * The React Query hooks call ONLY these functions, so switching modes needs no
 * component changes.
 */
import {
  api,
  setApiBaseUrl,
  getUniverse as apiGetUniverse,
  pappersStatus as apiPappersStatus,
  pappersSearch as apiPappersSearch,
  pappersCompany as apiPappersCompany,
} from './api.js';
import store from './localStore.js';
import engine from './localEngine.js';

// --- mode / server-url settings (client-side) ------------------------------
export function getMode() {
  return store.getMode();
}

export function setMode(mode) {
  return store.setMode(mode);
}

export function getServerUrl() {
  return store.getServerUrl();
}

export function setServerUrl(url) {
  const saved = store.setServerUrl(url);
  setApiBaseUrl(saved);
  return saved;
}

/** Ensure axios points at the configured server before any remote call. */
function syncRemoteBase() {
  setApiBaseUrl(store.getServerUrl());
}

const isLocal = () => getMode() === 'local';

/** Seed the offline store on first use so the dashboard is never empty. */
function seededEngine() {
  engine.ensureSeed();
  return engine;
}

/**
 * One-time init at app start: point axios at any saved server URL and, in
 * offline mode, seed the store so the dashboard is populated immediately.
 */
export function initDataProvider() {
  try {
    setApiBaseUrl(store.getServerUrl());
    if (isLocal()) engine.ensureSeed();
  } catch {
    /* never block app startup */
  }
}

// --- read operations -------------------------------------------------------
export async function getHealth() {
  if (isLocal()) {
    const e = seededEngine();
    return { ...e.health(), mode: 'local' };
  }
  syncRemoteBase();
  const { data } = await api.get('/health');
  return data;
}

export async function getAssets() {
  if (isLocal()) return seededEngine().listAssets({ limit: 200 });
  syncRemoteBase();
  const { data } = await api.get('/assets');
  return Array.isArray(data) ? data : [];
}

export async function getAsset(ticker) {
  if (isLocal()) return seededEngine().getAsset(ticker);
  syncRemoteBase();
  const { data } = await api.get(`/assets/${encodeURIComponent(ticker)}`);
  return data;
}

export async function getTheses(limit = 50) {
  if (isLocal()) return seededEngine().listTheses(limit);
  syncRemoteBase();
  const { data } = await api.get('/theses', { params: { limit } });
  return Array.isArray(data) ? data : [];
}

export async function getThesis(id) {
  if (isLocal()) return seededEngine().getThesis(id);
  syncRemoteBase();
  const { data } = await api.get(`/theses/${encodeURIComponent(id)}`);
  return data;
}

export async function getScans() {
  if (isLocal()) return seededEngine().listScans();
  syncRemoteBase();
  const { data } = await api.get('/scans');
  return Array.isArray(data) ? data : [];
}

export async function getScan(id) {
  if (isLocal()) return seededEngine().getScan(id);
  syncRemoteBase();
  const { data } = await api.get(`/scans/${encodeURIComponent(id)}`);
  return data;
}

export async function getUniverse() {
  if (isLocal()) return seededEngine().getUniverse();
  syncRemoteBase();
  return apiGetUniverse();
}

export async function createScan({ tickers, label, sector, domain } = {}) {
  if (isLocal()) return engine.createScan({ tickers, label, sector, domain });
  syncRemoteBase();
  const payload = {};
  if (tickers && tickers.length) payload.tickers = tickers;
  if (label) payload.label = label;
  if (sector) payload.sector = sector;
  if (domain) payload.domain = domain;
  const { data } = await api.post('/scans', payload);
  return data;
}

export async function getWatchlist() {
  if (isLocal()) return seededEngine().listWatchlist();
  syncRemoteBase();
  const { data } = await api.get('/watchlist');
  return Array.isArray(data) ? data : [];
}

export async function addWatchlist({ ticker, note }) {
  if (isLocal()) return engine.addWatchlist({ ticker, note });
  syncRemoteBase();
  const { data } = await api.post('/watchlist', { ticker, note });
  return data;
}

export async function removeWatchlist(ticker) {
  if (isLocal()) return engine.removeWatchlist(ticker);
  syncRemoteBase();
  const { data } = await api.delete(`/watchlist/${encodeURIComponent(ticker)}`);
  return data;
}

export async function getReports(limit = 50) {
  if (isLocal()) return seededEngine().listReports({ limit });
  syncRemoteBase();
  const { data } = await api.get('/reports', { params: { limit } });
  return Array.isArray(data) ? data : [];
}

export async function getConfig() {
  if (isLocal()) return engine.getConfig();
  syncRemoteBase();
  const { data } = await api.get('/config');
  return data;
}

export async function updateConfig(partial) {
  if (isLocal()) return engine.updateConfig(partial);
  syncRemoteBase();
  const { data } = await api.put('/config', partial);
  return data;
}

export async function getLogs(limit = 100) {
  if (isLocal()) return seededEngine().listLogs({ limit });
  syncRemoteBase();
  const { data } = await api.get('/logs', { params: { limit } });
  return Array.isArray(data) ? data : [];
}

// --- Pappers (entreprises françaises) --------------------------------------
// En mode hors-ligne, l'engine renvoie toujours { available:false } — jamais de
// fausses entreprises. En mode serveur, on interroge la vraie API Pappers.
export async function pappersStatus() {
  if (isLocal()) return engine.pappersStatus();
  syncRemoteBase();
  return apiPappersStatus();
}

export async function pappersSearch(q) {
  if (isLocal()) return engine.pappersSearch(q);
  syncRemoteBase();
  return apiPappersSearch(q);
}

export async function pappersCompany(siren) {
  if (isLocal()) return engine.pappersCompany(siren);
  syncRemoteBase();
  return apiPappersCompany(siren);
}

export default {
  getMode,
  setMode,
  getServerUrl,
  setServerUrl,
  getHealth,
  getAssets,
  getAsset,
  getTheses,
  getThesis,
  getScans,
  getScan,
  createScan,
  getUniverse,
  getWatchlist,
  addWatchlist,
  removeWatchlist,
  getReports,
  getConfig,
  updateConfig,
  getLogs,
  pappersStatus,
  pappersSearch,
  pappersCompany,
};
