import PropTypes from 'prop-types';
import { FlaskConical, ShieldCheck } from 'lucide-react';

/**
 * Data-source badge: honestly signals whether the displayed data is real
 * (fetched from a live provider) or simulated (produced by the on-device
 * engine). Fail-safe: renders nothing when the source is absent/unknown.
 *
 * @param {{ source?: string|null, provider?: string|null, className?: string }} props
 */
export default function SourceBadge({ source, provider, className = '' }) {
  const s = source ? String(source).toLowerCase() : '';

  if (s === 'real') {
    const label = provider ? `Données réelles · ${provider}` : 'Données réelles';
    return (
      <span
        className={`pill bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/25 ${className}`}
        aria-label={label}
        title={label}
      >
        <ShieldCheck className="h-3.5 w-3.5" aria-hidden="true" />
        {label}
      </span>
    );
  }

  if (s === 'simulated') {
    const label = 'Données simulées';
    return (
      <span
        className={`pill bg-amber-500/10 text-amber-300 ring-1 ring-amber-500/25 ${className}`}
        aria-label={provider ? `${label} · ${provider}` : label}
        title={provider ? `${label} · ${provider}` : label}
      >
        <FlaskConical className="h-3.5 w-3.5" aria-hidden="true" />
        {label}
      </span>
    );
  }

  // Unknown / absent source: render nothing (fail-safe).
  return null;
}

SourceBadge.propTypes = {
  source: PropTypes.string,
  provider: PropTypes.string,
  className: PropTypes.string,
};
