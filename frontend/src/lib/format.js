/**
 * Formatting + conviction-color helpers shared across the app.
 */

/**
 * Map a 0-100 conviction score to a semantic tier.
 * @param {number|null|undefined} score
 * @returns {'high'|'mid'|'low'|'none'}
 */
export function convictionTier(score) {
  if (score === null || score === undefined || Number.isNaN(Number(score))) return 'none';
  const s = Number(score);
  if (s >= 70) return 'high';
  if (s >= 40) return 'mid';
  return 'low';
}

/** Tailwind/hex color tokens keyed by tier. */
export const TIER_TOKENS = {
  high: { hex: '#10b981', text: 'text-emerald-400', bg: 'bg-emerald-500/10', ring: 'ring-emerald-500/30', stroke: 'stroke-emerald-400' },
  mid: { hex: '#f59e0b', text: 'text-amber-400', bg: 'bg-amber-500/10', ring: 'ring-amber-500/30', stroke: 'stroke-amber-400' },
  low: { hex: '#f43f5e', text: 'text-rose-400', bg: 'bg-rose-500/10', ring: 'ring-rose-500/30', stroke: 'stroke-rose-400' },
  none: { hex: '#64748b', text: 'text-slate-400', bg: 'bg-slate-500/10', ring: 'ring-slate-500/30', stroke: 'stroke-slate-400' },
};

/**
 * Color tokens for a given score.
 * @param {number|null|undefined} score
 */
export function scoreColor(score) {
  return TIER_TOKENS[convictionTier(score)];
}

/**
 * Format a large currency number compactly (e.g. 1.2B).
 * @param {number|null|undefined} value
 * @param {string} [currency='USD']
 */
export function formatMarketCap(value, currency = 'USD') {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '—';
  try {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: currency || 'USD',
      notation: 'compact',
      maximumFractionDigits: 1,
    }).format(Number(value));
  } catch {
    return `${Number(value).toLocaleString()} ${currency || ''}`.trim();
  }
}

/**
 * Format a price value.
 * @param {number|null|undefined} value
 * @param {string} [currency='USD']
 */
export function formatPrice(value, currency = 'USD') {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '—';
  try {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: currency || 'USD',
      maximumFractionDigits: 2,
    }).format(Number(value));
  } catch {
    return `${Number(value).toFixed(2)} ${currency || ''}`.trim();
  }
}

/**
 * Format a monetary amount in euros, compactly, in French locale.
 * Examples: 12_400_000 -> "12,4 M€", 850_000 -> "850,0 k€", 1_200_000_000 -> "1,2 Md€".
 * @param {number|null|undefined} value
 * @returns {string}
 */
export function formatEuroCompact(value) {
  if (value === null || value === undefined || value === '' || Number.isNaN(Number(value))) {
    return '—';
  }
  const n = Number(value);
  const abs = Math.abs(n);
  const sign = n < 0 ? '-' : '';
  const nf = (v, max = 1) =>
    new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 0, maximumFractionDigits: max }).format(v);
  if (abs >= 1e9) return `${sign}${nf(abs / 1e9)} Md€`;
  if (abs >= 1e6) return `${sign}${nf(abs / 1e6)} M€`;
  if (abs >= 1e3) return `${sign}${nf(abs / 1e3)} k€`;
  return `${sign}${nf(abs, 0)} €`;
}

/**
 * Format an exact monetary amount in euros, French locale (e.g. "12 400 000 €").
 * @param {number|null|undefined} value
 * @returns {string}
 */
export function formatEuro(value) {
  if (value === null || value === undefined || value === '' || Number.isNaN(Number(value))) {
    return '—';
  }
  try {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
      maximumFractionDigits: 0,
    }).format(Number(value));
  } catch {
    return `${Number(value).toLocaleString('fr-FR')} €`;
  }
}

/**
 * Human-friendly relative time (e.g. "3h ago"). Falls back to a date string.
 * @param {string|number|Date|null|undefined} input
 */
export function timeAgo(input) {
  if (!input) return '—';
  const then = new Date(input).getTime();
  if (Number.isNaN(then)) return '—';
  const diff = Date.now() - then;
  const sec = Math.round(diff / 1000);
  if (sec < 60) return sec <= 1 ? "à l'instant" : `il y a ${sec} s`;
  const min = Math.round(sec / 60);
  if (min < 60) return `il y a ${min} min`;
  const hr = Math.round(min / 60);
  if (hr < 24) return `il y a ${hr} h`;
  const day = Math.round(hr / 24);
  if (day < 30) return `il y a ${day} j`;
  return new Date(input).toLocaleDateString('fr-FR');
}

/**
 * Absolute date-time string, safe against bad input.
 * @param {string|number|Date|null|undefined} input
 */
export function formatDateTime(input) {
  if (!input) return '—';
  const d = new Date(input);
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleString('fr-FR');
}

/** Clamp a number into [min,max]. */
export function clamp(value, min = 0, max = 100) {
  const n = Number(value);
  if (Number.isNaN(n)) return min;
  return Math.min(max, Math.max(min, n));
}

/** Round a score for display; returns '—' for missing. */
export function displayScore(score) {
  if (score === null || score === undefined || Number.isNaN(Number(score))) return '—';
  return Math.round(Number(score));
}

/** Sub-score weightings used across thesis breakdowns (French display labels). */
export const SUBSCORE_META = [
  { key: 'financial_score', label: 'Financier', weight: 25 },
  { key: 'business_score', label: 'Business', weight: 20 },
  { key: 'future_score', label: 'Futur', weight: 20 },
  { key: 'contrarian_score', label: 'Contrarien', weight: 15 },
  { key: 'geopolitical_score', label: 'Géopolitique', weight: 20 },
];

/** Known agent roles for the reports feed filter (French display labels). */
export const AGENT_ROLES = [
  { key: 'financial', label: 'Financier' },
  { key: 'business', label: 'Business' },
  { key: 'future', label: 'Futur' },
  { key: 'contrarian', label: 'Contrarien' },
  { key: 'geopolitics', label: 'Géopolitique' },
];
