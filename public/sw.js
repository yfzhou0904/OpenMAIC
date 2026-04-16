const CACHE_NAME = 'instaclass-v1';

self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(clients.claim());
});

self.addEventListener('fetch', (event) => {
  // Network-first strategy — this is a dynamic app, not suited for aggressive caching
  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request))
  );
});
