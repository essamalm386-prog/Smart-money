/**
 * Geopolitical / geostrategic analysis (JS port of
 * `backend/app/services/geopolitics.py`).
 *
 * Estimates how exposed an asset is to geopolitical forces (country risk, sector
 * sensitivity to conflict / sanctions / supply chains, and a synthetic news
 * tension signal) in order to anticipate FUTURE fluctuations. Produces:
 *  - geo_score (0-100): higher = more geopolitically favorable / resilient.
 *  - risk_level (Faible | Modéré | Élevé): headline risk read.
 *  - drivers: short French bullet reasons.
 *
 * Offline the tension is a deterministic synthetic signal derived from the same
 * sha256 seed as the backend, so scans are fully reproducible.
 */
import { seed01 } from './hash.js';
import { clamp } from './scoring.js';

// Country stability (0-100, higher = more stable / lower political risk).
export const COUNTRY_STABILITY = {
  'United States': 78,
  'United States of America': 78,
  USA: 78,
  France: 75,
  Germany: 77,
  'United Kingdom': 74,
  Netherlands: 80,
  Switzerland: 88,
  Canada: 82,
  Japan: 80,
  'South Korea': 66,
  Taiwan: 52,
  China: 55,
  'Hong Kong': 58,
  India: 62,
  Brazil: 58,
  Mexico: 57,
  Israel: 55,
  'Saudi Arabia': 56,
  Russia: 30,
  Ukraine: 28,
  Ireland: 82,
  Sweden: 84,
  Denmark: 85,
  Australia: 82,
};
export const DEFAULT_STABILITY = 62;

// Sectors highly sensitive to geopolitics (both risk AND opportunity).
export const SECTOR_SENSITIVITY = {
  Energy: 0.9,
  Énergie: 0.9,
  Technology: 0.7,
  Technologie: 0.7,
  Semiconductors: 0.95,
  'Basic Materials': 0.8,
  Materials: 0.8,
  Matériaux: 0.8,
  Industrials: 0.7,
  Industrie: 0.7,
  'Aerospace & Defense': 0.9,
  Defense: 0.9,
  Défense: 0.9,
  Utilities: 0.6,
  'Services publics': 0.6,
  'Financial Services': 0.5,
  Finance: 0.5,
  Healthcare: 0.4,
  Santé: 0.4,
  'Consumer Defensive': 0.4,
  'Consumer Cyclical': 0.5,
};

// Sectors that structurally BENEFIT from current geostrategic trends
// (reshoring, energy security, defense spending, chip sovereignty).
export const SECTOR_TAILWIND = {
  Energy: 8,
  Énergie: 8,
  Semiconductors: 10,
  Technology: 5,
  'Aerospace & Defense': 10,
  Defense: 10,
  Défense: 10,
  'Basic Materials': 6,
  Materials: 6,
  Matériaux: 6,
  Industrials: 4,
};

function round(value, n = 2) {
  const f = 10 ** n;
  return Math.round(value * f) / f;
}

function round3(value) {
  return Math.round(value * 1000) / 1000;
}

function riskLevel(score) {
  if (score >= 66) return 'Faible';
  if (score >= 45) return 'Modéré';
  return 'Élevé';
}

/** Deterministic synthetic tension in [0,1] — mirrors the backend seed. */
function syntheticTension(ticker, country) {
  return round3(seed01('geo' + ticker + (country || '')));
}

/**
 * Compute the geopolitical signal for an asset (offline / synthetic).
 * @param {string} ticker
 * @param {string|null} country
 * @param {string|null} sector
 * @returns {object} GeoSignal-shaped object.
 */
export function analyzeGeopolitics(ticker, country, sector) {
  const t = String(ticker || '').trim().toUpperCase();
  const stability = COUNTRY_STABILITY[(country || '').trim()] ?? DEFAULT_STABILITY;
  const sensitivity = SECTOR_SENSITIVITY[(sector || '').trim()] ?? 0.6;
  const tailwind = SECTOR_TAILWIND[(sector || '').trim()] ?? 0;

  const tension = syntheticTension(t, country);

  // More sensitive sectors are more affected by tension (positive or negative).
  const tensionPenalty = tension * 100 * sensitivity;
  let geoScore = clamp(
    0.55 * stability + 0.3 * (100 - tensionPenalty) + tailwind + 0.15 * 50
  );
  geoScore = round(clamp(geoScore), 2);
  const level = riskLevel(geoScore);

  const drivers = [];
  drivers.push(`Stabilité pays (${country || 'n/d'}) : ${stability}/100`);
  if (sensitivity >= 0.8) {
    drivers.push('Secteur très exposé aux tensions géopolitiques');
  }
  if (tailwind >= 6) {
    drivers.push(
      'Secteur porté par les tendances géostratégiques (souveraineté, défense, énergie)'
    );
  }
  if (tension >= 0.5) {
    drivers.push('Actualité géopolitique tendue récemment');
  } else if (tension <= 0.2) {
    drivers.push('Environnement géopolitique calme');
  }

  return {
    ticker: t,
    country: country ?? null,
    sector: sector ?? null,
    geo_score: geoScore,
    risk_level: level,
    tension,
    drivers,
    source: 'synthetic',
  };
}

export default analyzeGeopolitics;
