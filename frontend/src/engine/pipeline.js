/**
 * Analysis pipeline (JS port of `backend/app/agents/pipeline.py`).
 * Orchestrates the five analysts into a single conviction thesis per ticker.
 */
import { syntheticFundamentals } from './marketData.js';
import { syntheticMediaSignal } from './search.js';
import { analyzeGeopolitics } from './geopolitics.js';
import {
  quantAnalysis,
  businessAnalysis,
  futureAnalysis,
  contrarianAnalysis,
  geopoliticalAnalysis,
} from './analyzers.js';
import { convictionScore, recommendationFor } from './scoring.js';

function fmt0(value) {
  return String(Math.round(value));
}

function heuristicNarrative(name, ticker, rec, conviction, outputs) {
  const top = outputs.reduce((a, b) => (b.score > a.score ? b : a));
  const weak = outputs.reduce((a, b) => (b.score < a.score ? b : a));
  const thesis =
    `${name} (${ticker}) scores ${fmt0(conviction)}/100 conviction (${rec}). ` +
    `Strongest dimension: ${top.role} (${fmt0(top.score)}). ` +
    `Weakest dimension: ${weak.role} (${fmt0(weak.score)}).`;
  const risks = `Primary risk stems from ${weak.role.toLowerCase()} (${weak.summary})`;
  const catalysts = `Upside catalyst from ${top.role.toLowerCase()} (${top.summary})`;
  return { thesis, risks, catalysts };
}

/**
 * Derive a directional outlook + expected volatility (French labels).
 * Port of `backend/app/agents/pipeline.py::_fluctuation_forecast`.
 * @param {number} future
 * @param {number} contrarian
 * @param {number} geo
 * @param {number|null} beta
 * @returns {{outlook:string, volatility:string}}
 */
export function fluctuationForecast(future, contrarian, geo, beta) {
  // Directional bias: forward potential + re-rating room, tempered by geo risk.
  const direction = 0.45 * future + 0.25 * contrarian + 0.3 * geo;
  let outlook;
  if (direction >= 62) outlook = 'Haussière';
  else if (direction >= 45) outlook = 'Neutre';
  else outlook = 'Prudente';

  // Expected volatility: high beta + low geo score + high contrarian surprise.
  const b = beta != null ? beta : 1.0;
  const volIndex = (b - 1.0) * 40 + (100 - geo) * 0.5 + (100 - contrarian) * 0.2;
  let volatility;
  if (volIndex >= 45) volatility = 'Élevée';
  else if (volIndex >= 20) volatility = 'Modérée';
  else volatility = 'Faible';

  return { outlook, volatility };
}

/**
 * Run the full five-analyst analysis for a single ticker, fully on-device.
 * @param {string} rawTicker
 * @returns {object} AnalysisResult-shaped object.
 */
export function analyzeAsset(rawTicker) {
  const ticker = String(rawTicker || '').trim().toUpperCase();

  const fundamentals = syntheticFundamentals(ticker);
  const media = syntheticMediaSignal(ticker);
  const geoSignal = analyzeGeopolitics(
    ticker,
    fundamentals.country,
    fundamentals.sector
  );

  const quant = quantAnalysis(fundamentals);
  const business = businessAnalysis(fundamentals);
  const future = futureAnalysis(fundamentals);
  const contrarian = contrarianAnalysis(fundamentals, media);
  const geopolitical = geopoliticalAnalysis(geoSignal);
  const outputs = [quant, business, future, contrarian, geopolitical];

  const sub = {
    financial: quant.score,
    business: business.score,
    future: future.score,
    contrarian: contrarian.score,
    geopolitics: geopolitical.score,
  };
  const conviction = convictionScore(sub);
  const rec = recommendationFor(conviction);
  const { outlook, volatility } = fluctuationForecast(
    future.score,
    contrarian.score,
    geopolitical.score,
    fundamentals.beta
  );

  const name = fundamentals.name || ticker;
  const engine = 'heuristic';
  const narrative = heuristicNarrative(name, ticker, rec, conviction, outputs);

  const summary =
    `${rec} — conviction ${fmt0(conviction)}/100 ` +
    `(fin ${fmt0(sub.financial)} / biz ${fmt0(sub.business)} / ` +
    `fut ${fmt0(sub.future)} / contra ${fmt0(sub.contrarian)} / ` +
    `géo ${fmt0(sub.geopolitics)}) · prévision ${outlook.toLowerCase()}, ` +
    `volatilité ${volatility.toLowerCase()}.`;

  return {
    ticker,
    name,
    fundamentals,
    financial_score: sub.financial,
    business_score: sub.business,
    future_score: sub.future,
    contrarian_score: sub.contrarian,
    geopolitical_score: sub.geopolitics,
    geo_risk: geoSignal.risk_level,
    conviction_score: conviction,
    recommendation: rec,
    outlook,
    volatility,
    engine,
    summary,
    thesis: narrative.thesis,
    risks: narrative.risks,
    catalysts: narrative.catalysts,
    agent_outputs: outputs,
  };
}

export default analyzeAsset;
