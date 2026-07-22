import PropTypes from 'prop-types';
import { clamp, displayScore, scoreColor, convictionTier } from '../lib/format';

const SIZES = {
  sm: { d: 56, stroke: 5, font: 'text-sm' },
  md: { d: 88, stroke: 7, font: 'text-xl' },
  lg: { d: 132, stroke: 9, font: 'text-3xl' },
};

const TIER_LABEL = { high: 'Conviction élevée', mid: 'Conviction modérée', low: 'Conviction faible', none: 'Aucun score' };

/**
 * Circular conviction gauge (0-100) with tiered color.
 * @param {{ score?: number|null, size?: 'sm'|'md'|'lg', label?: boolean, className?: string }} props
 */
export default function ConvictionGauge({ score = null, size = 'md', label = true, className = '' }) {
  const cfg = SIZES[size] || SIZES.md;
  const value = clamp(score ?? 0, 0, 100);
  const tier = convictionTier(score);
  const tokens = scoreColor(score);
  const radius = (cfg.d - cfg.stroke) / 2;
  const circumference = 2 * Math.PI * radius;
  const dash = (value / 100) * circumference;

  return (
    <div
      className={`inline-flex flex-col items-center gap-1 ${className}`}
      role="img"
      aria-label={`Score de conviction ${displayScore(score)} sur 100. ${TIER_LABEL[tier]}.`}
    >
      <div className="relative" style={{ width: cfg.d, height: cfg.d }}>
        <svg width={cfg.d} height={cfg.d} className="-rotate-90" aria-hidden="true">
          <circle
            cx={cfg.d / 2}
            cy={cfg.d / 2}
            r={radius}
            fill="none"
            strokeWidth={cfg.stroke}
            className="stroke-slate-700/60"
          />
          <circle
            cx={cfg.d / 2}
            cy={cfg.d / 2}
            r={radius}
            fill="none"
            strokeWidth={cfg.stroke}
            strokeLinecap="round"
            stroke={tokens.hex}
            strokeDasharray={`${dash} ${circumference}`}
            style={{ transition: 'stroke-dasharray 0.6s ease' }}
          />
        </svg>
        <div className="absolute inset-0 flex items-center justify-center">
          <span className={`font-mono font-bold ${cfg.font} ${tokens.text}`}>{displayScore(score)}</span>
        </div>
      </div>
      {label ? <span className="text-[11px] uppercase tracking-wide text-slate-400">Conviction</span> : null}
    </div>
  );
}

ConvictionGauge.propTypes = {
  score: PropTypes.number,
  size: PropTypes.oneOf(['sm', 'md', 'lg']),
  label: PropTypes.bool,
  className: PropTypes.string,
};
