/**
 * Deterministic analytical engine (JS port of
 * `backend/app/agents/analyzers.py`). Four analysts each turn raw inputs into a
 * bounded sub-score (0-100), a short rationale, and a structured payload.
 */
import { clamp } from './scoring.js';

function round(value, n = 2) {
  const f = 10 ** n;
  return Math.round(value * f) / f;
}

/** Format like Python's `{x:.0f}` (integer, rounded). */
function fmt0(value) {
  return String(Math.round(value));
}

/**
 * Map a numeric value to points via ascending thresholds.
 * Neutral (middle point) when the value is missing.
 */
export function scoreBand(value, thresholds, points) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return points[Math.floor(points.length / 2)];
  }
  const n = Number(value);
  for (let i = 0; i < thresholds.length; i += 1) {
    if (n <= thresholds[i]) return points[i];
  }
  return points[points.length - 1];
}

// 1. QUANT ANALYST — financial quality --------------------------------------
export function quantAnalysis(f) {
  const signals = [];

  const valuation = scoreBand(f.pe_ratio, [10, 18, 25, 35, 50], [100, 82, 62, 40, 22, 8]);
  const rev = (f.revenue_growth || 0) * 100;
  const eps = (f.earnings_growth || 0) * 100;
  const growth = clamp(50 + rev * 1.3 + eps * 0.9);
  const fcf = f.free_cashflow || 0;
  let cashflow = fcf > 0 ? 78 : 32;
  if (fcf > 1e10) cashflow = 92;
  const debt = scoreBand(f.debt_to_equity, [30, 70, 120, 180, 260], [95, 80, 60, 42, 25, 12]);
  const margins = clamp(40 + (f.operating_margins || 0) * 160);

  const score = round(
    0.25 * valuation + 0.22 * growth + 0.2 * cashflow + 0.18 * debt + 0.15 * margins,
    2
  );

  if (valuation >= 62) signals.push('attractive valuation multiple');
  if (growth >= 60) signals.push('solid top/bottom-line growth');
  if (cashflow >= 78) signals.push('positive free cash-flow');
  if (debt < 45) signals.push('elevated leverage');
  if (margins >= 60) signals.push('healthy operating margins');

  const summary = `Financial quality ${fmt0(score)}/100 — ${
    signals.length ? signals.join(', ') : 'balanced fundamentals'
  }.`;

  return {
    role: 'Quant Analyst',
    score: clamp(score),
    summary,
    payload: {
      valuation,
      growth: round(growth, 1),
      cashflow,
      debt,
      margins: round(margins, 1),
      pe_ratio: f.pe_ratio,
      revenue_growth: f.revenue_growth,
      debt_to_equity: f.debt_to_equity,
      free_cashflow: f.free_cashflow,
    },
  };
}

// 2. BUSINESS ANALYST — moat, market, management, competitiveness -----------
export function businessAnalysis(f) {
  const signals = [];
  const moat = clamp(35 + (f.gross_margins || 0) * 90);
  const cap = f.market_cap || 0;
  let market;
  if (cap >= 1e12) market = 88;
  else if (cap >= 1e11) market = 78;
  else if (cap >= 1e10) market = 66;
  else if (cap >= 1e9) market = 54;
  else market = 40;
  const management = clamp(45 + (f.return_on_equity || 0) * 130);
  const competitiveness = clamp(40 + (f.profit_margins || 0) * 170);

  const score = round(
    0.3 * moat + 0.25 * market + 0.25 * management + 0.2 * competitiveness,
    2
  );

  if (moat >= 70) signals.push('wide-moat gross margins');
  if (market >= 75) signals.push('dominant market scale');
  if (management >= 65) signals.push('efficient capital allocation');
  if (competitiveness >= 65) signals.push('strong competitive profitability');

  const summary = `Business quality ${fmt0(score)}/100 — ${
    signals.length ? signals.join(', ') : 'average competitive position'
  }.`;

  return {
    role: 'Business Analyst',
    score: clamp(score),
    summary,
    payload: {
      moat: round(moat, 1),
      market,
      management: round(management, 1),
      competitiveness: round(competitiveness, 1),
      gross_margins: f.gross_margins,
      return_on_equity: f.return_on_equity,
      market_cap: f.market_cap,
    },
  };
}

// 3. FUTURE POTENTIAL — runway, forward valuation, analyst trajectory -------
export function futureAnalysis(f) {
  const signals = [];
  const rev = (f.revenue_growth || 0) * 100;
  const runway = clamp(45 + rev * 1.8);
  const peg = scoreBand(f.peg_ratio, [1.0, 1.5, 2.0, 3.0, 4.0], [96, 80, 62, 45, 28, 15]);
  let forward;
  if (f.forward_pe && f.pe_ratio && f.forward_pe < f.pe_ratio) {
    forward = 78;
    signals.push('earnings expected to expand');
  } else {
    forward = 50;
  }
  const score = round(0.45 * runway + 0.35 * peg + 0.2 * forward, 2);

  if (runway >= 65) signals.push('strong revenue runway');
  if (peg >= 80) signals.push('cheap on a growth-adjusted basis');

  const summary = `Future potential ${fmt0(score)}/100 — ${
    signals.length ? signals.join(', ') : 'moderate growth outlook'
  }.`;

  return {
    role: 'Future Potential',
    score: clamp(score),
    summary,
    payload: {
      runway: round(runway, 1),
      peg,
      forward,
      peg_ratio: f.peg_ratio,
      revenue_growth: f.revenue_growth,
    },
  };
}

// 4. CONTRARIAN ANALYST — media saturation, crowding, sentiment -------------
export function contrarianAnalysis(f, media) {
  const signals = [];
  const coverage = clamp((1.0 - media.saturation) * 100);
  const rec = f.recommendation_mean;
  let crowding;
  if (rec === null || rec === undefined) {
    crowding = 55.0;
  } else {
    crowding = clamp(100 - Math.abs(rec - 2.8) * 40);
  }
  const score = round(0.65 * coverage + 0.35 * crowding, 2);

  if (coverage >= 65) signals.push('under the radar / low media saturation');
  else signals.push('already widely covered');
  if (rec !== null && rec !== undefined && rec > 2.8) {
    signals.push('street still cautious (room to re-rate)');
  }

  const summary = `Contrarian edge ${fmt0(score)}/100 — ${signals.join(', ')} (${
    media.article_count
  } recent articles).`;

  return {
    role: 'Contrarian Analyst',
    score: clamp(score),
    summary,
    payload: {
      coverage: round(coverage, 1),
      crowding: round(crowding, 1),
      saturation: media.saturation,
      article_count: media.article_count,
      headlines: media.headlines,
    },
  };
}

// 5. ANALYSTE GÉOPOLITIQUE — country risk, sector sensitivity, tension --------
export function geopoliticalAnalysis(geo) {
  const summary =
    `Perspective géopolitique ${fmt0(geo.geo_score)}/100 — ` +
    `risque ${String(geo.risk_level).toLowerCase()} : ` +
    geo.drivers.slice(0, 3).join(' ; ') +
    '.';

  return {
    role: 'Analyste Géopolitique',
    score: clamp(geo.geo_score),
    summary,
    payload: {
      risk_level: geo.risk_level,
      tension: geo.tension,
      country: geo.country,
      sector: geo.sector,
      drivers: geo.drivers,
      source: geo.source,
    },
  };
}

export default {
  quantAnalysis,
  businessAnalysis,
  futureAnalysis,
  contrarianAnalysis,
  geopoliticalAnalysis,
  scoreBand,
};
