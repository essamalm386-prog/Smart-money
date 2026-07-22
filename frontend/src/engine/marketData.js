/**
 * On-device market-data generator.
 *
 * Port of `backend/app/services/market_data.py::_synthetic`. Produces a
 * deterministic, plausible fundamentals profile for any ticker so the scoring
 * pipeline can run fully offline. Same ticker -> same fundamentals every time.
 */
import { seed01 } from './hash.js';

const SECTORS = ['Technology', 'Healthcare', 'Consumer', 'Industrials', 'Energy'];

/** Round to `n` decimal places (matches Python's round() closely enough). */
function round(value, n = 2) {
  const f = 10 ** n;
  return Math.round(value * f) / f;
}

function reverse(str) {
  return str.split('').reverse().join('');
}

/**
 * Build a deterministic fundamentals profile for a ticker.
 * @param {string} rawTicker
 * @returns {object} Fundamentals-shaped object (source: 'synthetic').
 */
export function syntheticFundamentals(rawTicker) {
  const ticker = String(rawTicker || '').trim().toUpperCase();
  const r = seed01(ticker);
  const r2 = seed01(reverse(ticker));

  return {
    ticker,
    name: `${ticker} Corp.`,
    sector: SECTORS[Math.floor(r * 5) % 5],
    industry: 'Diversified',
    country: 'United States',
    currency: 'USD',
    market_cap: round(5.0e8 + r * 2.5e12, 2),
    last_price: round(20 + r * 480, 2),
    pe_ratio: round(8 + r * 45, 2),
    forward_pe: round(7 + r2 * 40, 2),
    peg_ratio: round(0.5 + r2 * 2.5, 2),
    price_to_book: round(1 + r * 12, 2),
    revenue_growth: round(-0.05 + r * 0.45, 4),
    earnings_growth: round(-0.1 + r2 * 0.6, 4),
    gross_margins: round(0.2 + r * 0.6, 4),
    operating_margins: round(0.05 + r2 * 0.35, 4),
    profit_margins: round(0.02 + r * 0.3, 4),
    free_cashflow: round(-1e8 + r2 * 5e10, 2),
    total_debt: round(r * 4e10, 2),
    total_cash: round(r2 * 3e10, 2),
    debt_to_equity: round(r * 180, 2),
    return_on_equity: round(-0.05 + r2 * 0.45, 4),
    beta: round(0.5 + r * 1.6, 2),
    recommendation_mean: round(1.5 + r * 3.0, 2),
    number_of_analysts: Math.floor(3 + r2 * 40),
    source: 'synthetic',
  };
}

export default syntheticFundamentals;
