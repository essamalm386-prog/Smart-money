/**
 * Registers the service worker at the site root (`/sw.js`).
 * Guarded so it never breaks dev or unsupported browsers.
 */
export function registerServiceWorker() {
  if (typeof window === 'undefined' || !('serviceWorker' in navigator)) return;

  // Only auto-register in production builds; opt in during dev via ?sw=1.
  const isProd = import.meta.env.PROD;
  const forceDev = new URLSearchParams(window.location.search).has('sw');
  if (!isProd && !forceDev) return;

  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('/sw.js', { scope: '/' })
      .catch((err) => {
        // Non-fatal: app still works without offline support.
        console.warn('Service worker registration failed:', err);
      });
  });
}
