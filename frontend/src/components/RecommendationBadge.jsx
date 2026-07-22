import PropTypes from 'prop-types';
import { recommendationLabel } from '../lib/labels';

/**
 * Map a free-form recommendation string to a semantic style.
 * @param {string} rec
 */
function styleFor(rec) {
  const r = (rec || '').toLowerCase();
  if (r.includes('strong buy') || r.includes('strong_buy') || r.includes('conviction buy')) {
    return 'bg-emerald-500/15 text-emerald-300 ring-1 ring-emerald-500/30';
  }
  if (r.includes('buy') || r.includes('accumulate') || r.includes('overweight')) {
    return 'bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/20';
  }
  if (r.includes('hold') || r.includes('neutral') || r.includes('watch')) {
    return 'bg-amber-500/10 text-amber-300 ring-1 ring-amber-500/25';
  }
  if (
    r.includes('sell') ||
    r.includes('avoid') ||
    r.includes('reduce') ||
    r.includes('underweight') ||
    r.includes('short')
  ) {
    return 'bg-rose-500/10 text-rose-300 ring-1 ring-rose-500/25';
  }
  return 'bg-slate-500/10 text-slate-300 ring-1 ring-slate-500/25';
}

/**
 * Recommendation pill.
 * @param {{ recommendation?: string|null, className?: string }} props
 */
export default function RecommendationBadge({ recommendation, className = '' }) {
  const raw = recommendation && String(recommendation).trim() ? recommendation : '';
  const label = recommendationLabel(raw);
  return (
    <span className={`pill ${styleFor(raw)} ${className}`} title={`Recommandation : ${label}`}>
      {label}
    </span>
  );
}

RecommendationBadge.propTypes = {
  recommendation: PropTypes.string,
  className: PropTypes.string,
};
