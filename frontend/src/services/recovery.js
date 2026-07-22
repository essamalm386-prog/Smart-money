/**
 * Recovery helpers — always allow the user to get "unstuck".
 *
 * If the app was switched to server mode with a bad URL (or any bad config), the
 * user must always be able to return to a working offline state. These helpers
 * clear the offending settings and reload cleanly.
 */
import { KEYS } from './localStore.js';

function removeKey(key) {
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      window.localStorage.removeItem(key);
    }
  } catch {
    /* ignore */
  }
}

function setKey(key, value) {
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      window.localStorage.setItem(key, JSON.stringify(value));
    }
  } catch {
    /* ignore */
  }
}

function reloadClean() {
  try {
    if (typeof window !== 'undefined') {
      window.location.assign(window.location.pathname);
    }
  } catch {
    /* ignore */
  }
}

/** Force offline mode and drop the server URL, WITHOUT reloading. */
export function forceOffline() {
  setKey(KEYS.mode, 'local');
  removeKey(KEYS.serverUrl);
}

/** Wipe every app key (settings + cached data), WITHOUT reloading. */
export function clearAll() {
  Object.values(KEYS).forEach(removeKey);
}

/** Return to a working offline app and reload. */
export function resetToOffline() {
  forceOffline();
  reloadClean();
}

/** Full reset: clear all data + settings and reload as a fresh install. */
export function hardReset() {
  clearAll();
  reloadClean();
}

export default { forceOffline, clearAll, resetToOffline, hardReset };
