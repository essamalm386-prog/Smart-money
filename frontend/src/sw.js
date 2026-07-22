/* Smart Money — service worker (source of truth).
 *
 * NOTE: This file is copied verbatim to `public/sw.js` so that Vite serves it
 * at the site root (`/sw.js`), which is required for a root registration scope.
 * Edit this file, then run:  cp src/sw.js public/sw.js
 *
 * Responsibilities:
 *  - Precache the app shell for offline launch.
 *  - Runtime cache API GETs with stale-while-revalidate.
 *  - Offline fallback for navigations.
 *  - Push + notificationclick handlers (push-ready).
 */

const VERSION = 'v1';
const APP_SHELL_CACHE = `sm-shell-${VERSION}`;
const RUNTIME_CACHE = `sm-runtime-${VERSION}`;
const API_CACHE = `sm-api-${VERSION}`;

const APP_SHELL = [
  '/',
  '/index.html',
  '/offline.html',
  '/manifest.webmanifest',
  '/icons/icon.svg',
  '/icons/icon-192.png',
  '/icons/icon-512.png',
];

// ---- Install: precache app shell ----
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches
      .open(APP_SHELL_CACHE)
      .then((cache) => cache.addAll(APP_SHELL))
      .catch(() => {
        /* best-effort: don't fail install if an asset is missing */
      })
      .then(() => self.skipWaiting())
  );
});

// ---- Activate: clean up old caches ----
self.addEventListener('activate', (event) => {
  const keep = new Set([APP_SHELL_CACHE, RUNTIME_CACHE, API_CACHE]);
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((k) => !keep.has(k)).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

function isApiRequest(url) {
  return url.pathname.startsWith('/api/');
}

// stale-while-revalidate: return cached immediately, refresh in background
async function staleWhileRevalidate(request, cacheName) {
  const cache = await caches.open(cacheName);
  const cached = await cache.match(request);
  const network = fetch(request)
    .then((response) => {
      if (response && response.status === 200 && response.type !== 'opaque') {
        cache.put(request, response.clone());
      }
      return response;
    })
    .catch(() => undefined);
  return cached || network || Response.error();
}

// ---- Fetch routing ----
self.addEventListener('fetch', (event) => {
  const { request } = event;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);

  // Only handle same-origin requests
  if (url.origin !== self.location.origin) return;

  // API GETs -> stale-while-revalidate (network refresh in background)
  if (isApiRequest(url)) {
    event.respondWith(
      staleWhileRevalidate(request, API_CACHE).then(
        (res) => res || new Response(JSON.stringify({ error: 'offline' }), {
          status: 503,
          headers: { 'Content-Type': 'application/json' },
        })
      )
    );
    return;
  }

  // Navigations -> network first, fall back to cached shell, then offline page
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(APP_SHELL_CACHE).then((cache) => cache.put('/index.html', copy)).catch(() => {});
          return response;
        })
        .catch(async () => {
          const cache = await caches.open(APP_SHELL_CACHE);
          return (
            (await cache.match(request)) ||
            (await cache.match('/index.html')) ||
            (await cache.match('/offline.html')) ||
            Response.error()
          );
        })
    );
    return;
  }

  // Static assets -> stale-while-revalidate
  event.respondWith(staleWhileRevalidate(request, RUNTIME_CACHE));
});

// ---- Allow the page to trigger an immediate activation ----
self.addEventListener('message', (event) => {
  if (event.data === 'SKIP_WAITING') self.skipWaiting();
});

// ---- Push notifications (push-ready) ----
self.addEventListener('push', (event) => {
  let data = {};
  try {
    data = event.data ? event.data.json() : {};
  } catch (e) {
    data = { title: 'Smart Money', body: event.data ? event.data.text() : '' };
  }

  const title = data.title || 'Smart Money';
  const options = {
    body: data.body || 'New investment signal available.',
    icon: '/icons/icon-192.png',
    badge: '/icons/icon-192.png',
    tag: data.tag || 'smart-money',
    data: { url: data.url || '/' },
    vibrate: [80, 40, 80],
  };

  event.waitUntil(self.registration.showNotification(title, options));
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const target = (event.notification.data && event.notification.data.url) || '/';

  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if ('focus' in client) {
          client.navigate(target).catch(() => {});
          return client.focus();
        }
      }
      if (self.clients.openWindow) return self.clients.openWindow(target);
      return undefined;
    })
  );
});
