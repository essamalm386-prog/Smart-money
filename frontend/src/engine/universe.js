/**
 * Univers investissable — tickers regroupés par secteur et par thématique.
 *
 * Parité exacte avec le backend (`app/services/universe.py`) : mêmes clés,
 * mêmes libellés (en français) et mêmes listes de tickers. Alimente la
 * fonctionnalité « Découvrir où investir » en mode hors ligne : l'utilisateur
 * choisit un secteur (ex. Technologie) ou une thématique (ex. Intelligence
 * Artificielle) et le scan étend ce choix en une liste concrète de tickers.
 */

// --- Secteurs d'activité (style GICS), libellés français ------------------ //
export const SECTORS = {
  technologie: {
    label: 'Technologie',
    tickers: ['AAPL', 'MSFT', 'NVDA', 'AVGO', 'ORCL', 'CRM', 'ADBE', 'AMD', 'CSCO', 'TXN'],
  },
  services_communication: {
    label: 'Services de communication',
    tickers: ['GOOGL', 'META', 'NFLX', 'DIS', 'TMUS', 'T', 'VZ', 'SPOT'],
  },
  consommation_discretionnaire: {
    label: 'Consommation discrétionnaire',
    tickers: ['AMZN', 'TSLA', 'HD', 'NKE', 'MCD', 'SBUX', 'LOW', 'BKNG'],
  },
  consommation_base: {
    label: 'Consommation de base',
    tickers: ['PG', 'KO', 'PEP', 'COST', 'WMT', 'MDLZ', 'CL', 'MO'],
  },
  sante: {
    label: 'Santé',
    tickers: ['UNH', 'JNJ', 'LLY', 'MRK', 'ABBV', 'PFE', 'TMO', 'AMGN', 'ISRG'],
  },
  finance: {
    label: 'Finance',
    tickers: ['JPM', 'BAC', 'WFC', 'GS', 'MS', 'BLK', 'SCHW', 'V', 'MA', 'AXP'],
  },
  industrie: {
    label: 'Industrie',
    tickers: ['CAT', 'BA', 'HON', 'GE', 'UPS', 'RTX', 'DE', 'LMT', 'UNP'],
  },
  energie: {
    label: 'Énergie',
    tickers: ['XOM', 'CVX', 'COP', 'SLB', 'EOG', 'PSX', 'MPC', 'OXY'],
  },
  materiaux: {
    label: 'Matériaux',
    tickers: ['LIN', 'SHW', 'FCX', 'NEM', 'APD', 'ECL', 'DOW'],
  },
  immobilier: {
    label: 'Immobilier',
    tickers: ['PLD', 'AMT', 'EQIX', 'SPG', 'O', 'CCI', 'PSA'],
  },
  services_publics: {
    label: 'Services publics',
    tickers: ['NEE', 'DUK', 'SO', 'D', 'AEP', 'EXC', 'SRE'],
  },
};

// --- Thématiques / domaines d'investissement, libellés français ----------- //
export const DOMAINS = {
  ia: {
    label: 'Intelligence Artificielle',
    tickers: ['NVDA', 'MSFT', 'GOOGL', 'META', 'AMD', 'PLTR', 'SNOW', 'AI', 'SMCI'],
  },
  semi_conducteurs: {
    label: 'Semi-conducteurs',
    tickers: ['NVDA', 'AMD', 'AVGO', 'TSM', 'ASML', 'QCOM', 'MU', 'INTC', 'TXN', 'SMCI'],
  },
  cloud_logiciel: {
    label: 'Cloud & Logiciel',
    tickers: ['MSFT', 'CRM', 'ADBE', 'ORCL', 'NOW', 'SNOW', 'DDOG', 'NET', 'WDAY'],
  },
  cybersecurite: {
    label: 'Cybersécurité',
    tickers: ['PANW', 'CRWD', 'ZS', 'FTNT', 'S', 'OKTA', 'NET', 'CYBR'],
  },
  fintech: {
    label: 'Fintech & Paiements',
    tickers: ['V', 'MA', 'PYPL', 'SQ', 'COIN', 'SOFI', 'AXP', 'FI', 'GPN'],
  },
  energie_propre: {
    label: 'Énergie propre',
    tickers: ['ENPH', 'FSLR', 'SEDG', 'NEE', 'PLUG', 'RUN', 'BE', 'ICLN'],
  },
  sante_biotech: {
    label: 'Santé & Biotech',
    tickers: ['LLY', 'NVO', 'MRNA', 'VRTX', 'REGN', 'AMGN', 'ISRG', 'BIIB'],
  },
  vehicules_electriques: {
    label: 'Véhicules électriques & Mobilité',
    tickers: ['TSLA', 'RIVN', 'LCID', 'F', 'GM', 'NIO', 'BYDDY', 'CHPT'],
  },
  ecommerce: {
    label: 'E-commerce',
    tickers: ['AMZN', 'SHOP', 'MELI', 'SE', 'ETSY', 'BABA', 'PDD', 'EBAY'],
  },
  defense_aerospatiale: {
    label: 'Défense & Aérospatiale',
    tickers: ['LMT', 'RTX', 'NOC', 'GD', 'BA', 'LHX', 'HWM'],
  },
  luxe_marques: {
    label: 'Luxe & Grandes marques',
    tickers: ['LVMUY', 'NKE', 'SBUX', 'MC.PA', 'RMS.PA', 'EL', 'TPR'],
  },
};

/**
 * Renvoie les secteurs et thématiques avec libellés et nombre de valeurs,
 * dans le même format que `GET /api/universe`.
 * @returns {{ sectors: Array, domains: Array }}
 */
export function listUniverse() {
  return {
    sectors: Object.entries(SECTORS).map(([key, v]) => ({
      key,
      label: v.label,
      count: v.tickers.length,
      tickers: [...v.tickers],
    })),
    domains: Object.entries(DOMAINS).map(([key, v]) => ({
      key,
      label: v.label,
      count: v.tickers.length,
      tickers: [...v.tickers],
    })),
  };
}

const normKey = (key) => String(key || '').toLowerCase().trim();

/** Tickers d'un secteur (liste vide si la clé est inconnue). */
export function tickersForSector(key) {
  const entry = SECTORS[normKey(key)];
  return entry ? [...entry.tickers] : [];
}

/** Tickers d'une thématique (liste vide si la clé est inconnue). */
export function tickersForDomain(key) {
  const entry = DOMAINS[normKey(key)];
  return entry ? [...entry.tickers] : [];
}

/** Libellé français d'un secteur (renvoie la clé si inconnue). */
export function sectorLabel(key) {
  const entry = SECTORS[normKey(key)];
  return entry ? entry.label : key;
}

/** Libellé français d'une thématique (renvoie la clé si inconnue). */
export function domainLabel(key) {
  const entry = DOMAINS[normKey(key)];
  return entry ? entry.label : key;
}

export default {
  SECTORS,
  DOMAINS,
  listUniverse,
  tickersForSector,
  tickersForDomain,
  sectorLabel,
  domainLabel,
};
