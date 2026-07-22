/**
 * localStorage-backed persistence layer for the on-device (offline) engine.
 *
 * All data lives under namespaced JSON keys. Everything degrades gracefully:
 * if localStorage is unavailable (private mode / SSR) an in-memory fallback is
 * used so the app never crashes.
 */

export const KEYS = {
  assets: 'sm.assets',
  theses: 'sm.theses',
  scans: 'sm.scans',
  reports: 'sm.reports',
  watchlist: 'sm.watchlist',
  logs: 'sm.logs',
  history: 'sm.history',
  config: 'sm.config',
  mode: 'sm.mode',
  serverUrl: 'sm.serverUrl',
  seq: 'sm.seq',
};

// --- storage backend (localStorage with in-memory fallback) ---------------
const memory = new Map();

function backend() {
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      // Probe availability (throws in some locked-down contexts).
      const probe = '__sm_probe__';
      window.localStorage.setItem(probe, '1');
      window.localStorage.removeItem(probe);
      return window.localStorage;
    }
  } catch {
    /* fall through to memory */
  }
  return {
    getItem: (k) => (memory.has(k) ? memory.get(k) : null),
    setItem: (k, v) => memory.set(k, v),
    removeItem: (k) => memory.delete(k),
  };
}

/** Read + JSON.parse a key, returning `fallback` on any problem. */
export function read(key, fallback = null) {
  try {
    const raw = backend().getItem(key);
    if (raw === null || raw === undefined) return fallback;
    return JSON.parse(raw);
  } catch {
    return fallback;
  }
}

/** JSON.stringify + write a key. */
export function write(key, value) {
  try {
    backend().setItem(key, JSON.stringify(value));
  } catch {
    /* best-effort; ignore quota / serialization errors */
  }
  return value;
}

export function remove(key) {
  try {
    backend().removeItem(key);
  } catch {
    /* ignore */
  }
}

// --- collection helpers ----------------------------------------------------
/** Return a collection array (always an array). */
export function getCollection(key) {
  const v = read(key, []);
  return Array.isArray(v) ? v : [];
}

/** Persist a collection array. */
export function setCollection(key, arr) {
  return write(key, Array.isArray(arr) ? arr : []);
}

/** Auto-increment id per collection key. */
export function nextId(key) {
  const seq = read(KEYS.seq, {}) || {};
  const current = Number(seq[key] || 0) + 1;
  seq[key] = current;
  write(KEYS.seq, seq);
  return current;
}

/** Append an item (assigning an id when missing) and persist. */
export function insert(key, item) {
  const list = getCollection(key);
  const record = { id: item.id ?? nextId(key), ...item };
  list.push(record);
  setCollection(key, list);
  return record;
}

// --- simple scalar helpers -------------------------------------------------
export function getMode() {
  const m = read(KEYS.mode, 'local');
  return m === 'remote' ? 'remote' : 'local';
}

export function setMode(mode) {
  const m = mode === 'remote' ? 'remote' : 'local';
  write(KEYS.mode, m);
  return m;
}

export function getServerUrl() {
  return read(KEYS.serverUrl, '') || '';
}

export function setServerUrl(url) {
  write(KEYS.serverUrl, url || '');
  return url || '';
}

export default {
  KEYS,
  read,
  write,
  remove,
  getCollection,
  setCollection,
  nextId,
  insert,
  getMode,
  setMode,
  getServerUrl,
  setServerUrl,
};
