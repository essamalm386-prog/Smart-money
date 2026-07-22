/**
 * Display-label maps: translate raw (English) engine/backend values into French
 * for display ONLY. The underlying data values are never mutated.
 */

/** Raw recommendation value -> French display label. */
export const RECOMMENDATION_FR = {
  STRONG_BUY: 'ACHAT FORT',
  BUY: 'ACHETER',
  HOLD: 'CONSERVER',
  REDUCE: 'ALLÉGER',
  AVOID: 'ÉVITER',
};

/**
 * French label for a raw recommendation value (fail-safe: returns the raw
 * value if it is unknown, or a neutral label when empty).
 * @param {string|null|undefined} rec
 */
export function recommendationLabel(rec) {
  if (rec === null || rec === undefined || String(rec).trim() === '') return 'Non noté';
  const key = String(rec).trim().toUpperCase().replace(/[\s-]+/g, '_');
  return RECOMMENDATION_FR[key] || rec;
}

/** Raw agent-role value -> French display label (covers both naming schemes). */
export const ROLE_FR = {
  // Display names emitted by the on-device analyzers.
  'Quant Analyst': 'Analyste Quantitatif',
  'Business Analyst': 'Analyste Business',
  'Future Potential': 'Potentiel Futur',
  'Contrarian Analyst': 'Analyste Contrarien',
  // Already French — pass through unchanged.
  'Analyste Géopolitique': 'Analyste Géopolitique',
  'Investment Committee': "Comité d'Investissement",
  // Snake-case / short keys a backend may use.
  quant: 'Analyste Quantitatif',
  financial: 'Analyste Quantitatif',
  business: 'Analyste Business',
  future: 'Potentiel Futur',
  contrarian: 'Analyste Contrarien',
  geopolitics: 'Analyste Géopolitique',
  geopolitical: 'Analyste Géopolitique',
  committee: "Comité d'Investissement",
  investment_committee: "Comité d'Investissement",
};

/**
 * French label for a raw agent-role value (fail-safe: humanises unknown roles).
 * @param {string|null|undefined} role
 */
export function roleLabel(role) {
  if (role === null || role === undefined || String(role).trim() === '') return 'Agent';
  const raw = String(role).trim();
  if (ROLE_FR[raw]) return ROLE_FR[raw];
  const key = raw.toLowerCase().replace(/\s+/g, '_');
  if (ROLE_FR[key]) return ROLE_FR[key];
  return raw.replace(/_/g, ' ');
}
