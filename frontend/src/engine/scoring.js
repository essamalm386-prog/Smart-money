/**
 * Conviction scoring engine (JS port of `backend/app/services/scoring.py`).
 *
 * v2 model: geopolitics added as a 5th factor.
 * CONVICTION = 0.25*FINANCIAL + 0.20*BUSINESS + 0.20*FUTURE
 *            + 0.15*CONTRARIAN + 0.20*GEOPOLITICS
 * Each sub-score is 0-100; the blend is clamped to 0-100.
 */

/** Canonical weights (sum to 1.0). */
export const WEIGHTS = {
  financial: 0.25,
  business: 0.2,
  future: 0.2,
  contrarian: 0.15,
  geopolitics: 0.2,
};

/** Clamp a value into [low, high]. */
export function clamp(value, low = 0.0, high = 100.0) {
  const n = Number(value);
  if (Number.isNaN(n)) return low;
  return Math.max(low, Math.min(high, n));
}

function round2(value) {
  return Math.round(value * 100) / 100;
}

/**
 * Weighted conviction score (0-100) from the five clamped sub-scores.
 * @param {{financial:number, business:number, future:number, contrarian:number, geopolitics?:number}} sub
 * @returns {number}
 */
export function convictionScore(sub) {
  const s = {
    financial: clamp(sub.financial),
    business: clamp(sub.business),
    future: clamp(sub.future),
    contrarian: clamp(sub.contrarian),
    geopolitics: clamp(sub.geopolitics ?? 50),
  };
  const score =
    WEIGHTS.financial * s.financial +
    WEIGHTS.business * s.business +
    WEIGHTS.future * s.future +
    WEIGHTS.contrarian * s.contrarian +
    WEIGHTS.geopolitics * s.geopolitics;
  return round2(clamp(score));
}

/** Map a conviction score to a discrete recommendation label. */
export function recommendationFor(score) {
  if (score >= 80) return 'STRONG_BUY';
  if (score >= 65) return 'BUY';
  if (score >= 45) return 'HOLD';
  if (score >= 30) return 'REDUCE';
  return 'AVOID';
}

/** Minimum-quality gate mirroring the backend helper. */
export function qualityGate(marketCap, pe, minMarketCap, maxPe) {
  if (marketCap != null && marketCap < minMarketCap) return false;
  if (pe != null && pe > 0 && pe > maxPe) return false;
  return true;
}

export default convictionScore;
