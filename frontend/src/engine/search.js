/**
 * On-device media/sentiment signal generator.
 *
 * Port of `backend/app/services/search.py::_synthetic`. The contrarian analyst
 * rewards low media saturation, so this returns a deterministic article count
 * and saturation for any ticker. Same ticker -> same signal every time.
 */
import { seed01 } from './hash.js';

function round(value, n = 3) {
  const f = 10 ** n;
  return Math.round(value * f) / f;
}

/**
 * Build a deterministic media saturation signal for a ticker.
 * @param {string} rawTicker
 * @returns {{ticker:string, article_count:number, saturation:number, headlines:string[], source:string}}
 */
export function syntheticMediaSignal(rawTicker) {
  const ticker = String(rawTicker || '').trim().toUpperCase();
  const r = seed01(`media${ticker}`);
  const count = Math.floor(r * 60);
  return {
    ticker,
    article_count: count,
    saturation: round(Math.min(1.0, count / 50.0), 3),
    headlines: [
      `${ticker}: analysts weigh outlook`,
      `${ticker} in focus this quarter`,
    ],
    source: 'synthetic',
  };
}

export default syntheticMediaSignal;
