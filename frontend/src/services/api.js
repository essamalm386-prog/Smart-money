import axios from 'axios';

/**
 * Base URL for the FastAPI backend.
 * Defaults to '/api' so that a dev proxy (vite) or nginx can forward requests.
 */
export const API_BASE = import.meta.env.VITE_API_BASE || '/api';

/** Shared axios instance for all Smart Money API calls. */
export const api = axios.create({
  baseURL: API_BASE,
  timeout: 9000, // fail fast on a bad/unreachable server URL
  headers: { 'Content-Type': 'application/json' },
});

/**
 * Point the axios instance at a server URL (used in "Connect to server" mode).
 * Falls back to the compile-time API_BASE when no URL is provided.
 * @param {string} [url]
 */
export function setApiBaseUrl(url) {
  api.defaults.baseURL = url && url.trim() ? url.trim() : API_BASE;
  return api.defaults.baseURL;
}

/**
 * Normalize any axios/network error into a friendly, throwable Error.
 * @param {unknown} error
 * @returns {Error}
 */
export function toApiError(error) {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      const detail =
        error.response.data && (error.response.data.detail || error.response.data.error);
      return new Error(
        typeof detail === 'string'
          ? detail
          : `Request failed (${error.response.status})`
      );
    }
    if (error.request) {
      return new Error('Cannot reach the Smart Money backend. Check your connection.');
    }
  }
  return error instanceof Error ? error : new Error('Unexpected error');
}

// Reject with a normalized Error so React Query surfaces friendly messages.
api.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(toApiError(error))
);

/**
 * GET /universe -> { sectors: [...], domains: [...] }
 * Secteurs d'activité et thématiques d'investissement (libellés français).
 */
export async function getUniverse() {
  const { data } = await api.get('/universe');
  return data && typeof data === 'object' ? data : { sectors: [], domains: [] };
}

/**
 * POST /scans -> Scan. Transmet les champs optionnels sector/domain au backend
 * qui les étend en liste de tickers.
 * @param {{ tickers?: string[], label?: string, sector?: string, domain?: string }} body
 */
export async function createScan({ tickers, label, sector, domain } = {}) {
  const payload = {};
  if (tickers && tickers.length) payload.tickers = tickers;
  if (label) payload.label = label;
  if (sector) payload.sector = sector;
  if (domain) payload.domain = domain;
  const { data } = await api.post('/scans', payload);
  return data;
}

// --- Pappers (entreprises françaises, mode serveur uniquement) -------------

/**
 * GET /pappers/status -> { provider:"Pappers", configured: boolean }
 * Indique si une clé PAPPERS_API_KEY est configurée côté serveur.
 */
export async function pappersStatus() {
  const { data } = await api.get('/pappers/status');
  return data && typeof data === 'object'
    ? data
    : { provider: 'Pappers', configured: false };
}

/**
 * GET /pappers/search?q= -> résultats de recherche d'entreprises françaises.
 * Renvoie soit { available:true, results:[…] }, soit { available:false, reason }.
 * @param {string} q  Nom d'entreprise ou SIREN.
 */
export async function pappersSearch(q) {
  const { data } = await api.get('/pappers/search', { params: { q } });
  return data && typeof data === 'object'
    ? data
    : { available: false, provider: 'Pappers', reason: 'Réponse invalide du serveur.' };
}

/**
 * GET /pappers/company/{siren} -> fiche détaillée d'une entreprise.
 * Renvoie soit { available:true, company:{…} }, soit { available:false, reason }.
 * @param {string|number} siren
 */
export async function pappersCompany(siren) {
  const { data } = await api.get(`/pappers/company/${encodeURIComponent(siren)}`);
  return data && typeof data === 'object'
    ? data
    : { available: false, reason: 'Réponse invalide du serveur.' };
}

export default api;
