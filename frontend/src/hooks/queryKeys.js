/** Centralized React Query cache keys. */
export const qk = {
  health: ['health'],
  assets: ['assets'],
  asset: (ticker) => ['asset', ticker],
  theses: (limit) => ['theses', { limit }],
  thesis: (id) => ['thesis', id],
  scans: ['scans'],
  scan: (id) => ['scan', id],
  universe: ['universe'],
  watchlist: ['watchlist'],
  reports: (limit) => ['reports', { limit }],
  config: ['config'],
  logs: (limit) => ['logs', { limit }],
  pappersStatus: ['pappers', 'status'],
  pappersSearch: (q) => ['pappers', 'search', q],
  pappersCompany: (siren) => ['pappers', 'company', siren],
};
