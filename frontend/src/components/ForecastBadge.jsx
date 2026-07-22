import PropTypes from 'prop-types';
import { TrendingUp, Minus, TrendingDown, Compass } from 'lucide-react';
import { displayScore } from '../lib/format';

/**
 * Prévision de fluctuation : tendance (outlook), volatilité attendue et risque
 * géopolitique. Tout est fail-safe : si aucun champ n'est présent, on n'affiche
 * rien (retourne null).
 */

/** Style + icône pour la tendance. */
function outlookMeta(outlook) {
  const v = String(outlook || '').trim().toLowerCase();
  if (v.startsWith('hauss')) {
    return {
      label: outlook,
      Icon: TrendingUp,
      text: 'text-emerald-300',
      bg: 'bg-emerald-500/10',
      ring: 'ring-emerald-500/25',
      dot: 'bg-emerald-400',
    };
  }
  if (v.startsWith('prud')) {
    return {
      label: outlook,
      Icon: TrendingDown,
      text: 'text-amber-300',
      bg: 'bg-amber-500/10',
      ring: 'ring-amber-500/25',
      dot: 'bg-amber-400',
    };
  }
  // Neutre (par défaut).
  return {
    label: outlook || 'Neutre',
    Icon: Minus,
    text: 'text-slate-300',
    bg: 'bg-slate-500/10',
    ring: 'ring-slate-500/25',
    dot: 'bg-slate-400',
  };
}

/** Style (pastille) pour le risque géopolitique. */
function riskMeta(risk) {
  const v = String(risk || '').trim().toLowerCase();
  if (v.startsWith('faible')) {
    return { label: risk, text: 'text-emerald-300', dot: 'bg-emerald-400' };
  }
  if (v.startsWith('élev') || v.startsWith('elev')) {
    return { label: risk, text: 'text-rose-300', dot: 'bg-rose-400' };
  }
  // Modéré (par défaut).
  return { label: risk || 'Modéré', text: 'text-amber-300', dot: 'bg-amber-400' };
}

export default function ForecastBadge({
  outlook = null,
  volatility = null,
  geoRisk = null,
  geoScore = null,
  compact = false,
  className = '',
}) {
  const hasAny =
    (outlook && String(outlook).trim()) ||
    (volatility && String(volatility).trim()) ||
    (geoRisk && String(geoRisk).trim()) ||
    geoScore !== null && geoScore !== undefined;
  if (!hasAny) return null;

  const o = outlookMeta(outlook);
  const r = riskMeta(geoRisk);
  const { Icon } = o;

  // --- Compact : ligne discrète pour les cartes du tableau de bord ---------
  if (compact) {
    return (
      <div
        className={`flex flex-wrap items-center gap-x-3 gap-y-1 text-[11px] ${className}`}
        title="Prévision de fluctuation"
      >
        <span
          className={`inline-flex items-center gap-1 rounded-md px-1.5 py-0.5 font-medium ring-1 ${o.bg} ${o.text} ${o.ring}`}
        >
          <Icon className="h-3 w-3" aria-hidden="true" />
          {o.label}
        </span>
        {volatility ? (
          <span className="text-slate-400">
            Volatilité <span className="text-slate-300">{volatility}</span>
          </span>
        ) : null}
        {geoRisk ? (
          <span className="inline-flex items-center gap-1 text-slate-400">
            <span className={`h-2 w-2 rounded-full ${r.dot}`} aria-hidden="true" />
            <span className={r.text}>{r.label}</span>
          </span>
        ) : null}
      </div>
    );
  }

  // --- Détaillé : section mise en évidence sur la page AssetDetail ----------
  return (
    <section className={`card p-5 ${className}`}>
      <h2 className="mb-4 flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-slate-300">
        <Compass className="h-4 w-4 text-emerald-400" aria-hidden="true" /> Prévision &amp; géopolitique
      </h2>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <p className="text-[11px] uppercase tracking-wide text-slate-500">Tendance</p>
          <p className={`mt-1 inline-flex items-center gap-1.5 text-sm font-semibold ${o.text}`}>
            <Icon className="h-4 w-4" aria-hidden="true" />
            {o.label}
          </p>
        </div>
        <div>
          <p className="text-[11px] uppercase tracking-wide text-slate-500">Volatilité attendue</p>
          <p className="mt-1 text-sm font-semibold text-slate-200">{volatility || '—'}</p>
        </div>
        <div>
          <p className="text-[11px] uppercase tracking-wide text-slate-500">Risque géopolitique</p>
          <p className={`mt-1 inline-flex items-center gap-1.5 text-sm font-semibold ${r.text}`}>
            <span className={`h-2.5 w-2.5 rounded-full ${r.dot}`} aria-hidden="true" />
            {r.label}
          </p>
        </div>
        <div>
          <p className="text-[11px] uppercase tracking-wide text-slate-500">Score géopolitique</p>
          <p className="mt-1 font-mono text-sm font-semibold text-slate-200">
            {displayScore(geoScore)}
            {geoScore !== null && geoScore !== undefined ? (
              <span className="text-slate-500"> /100</span>
            ) : null}
          </p>
        </div>
      </div>
    </section>
  );
}

ForecastBadge.propTypes = {
  outlook: PropTypes.string,
  volatility: PropTypes.string,
  geoRisk: PropTypes.string,
  geoScore: PropTypes.number,
  compact: PropTypes.bool,
  className: PropTypes.string,
};
