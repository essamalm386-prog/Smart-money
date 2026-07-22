import PropTypes from 'prop-types';
import { clamp, displayScore, scoreColor } from '../lib/format';

/**
 * Horizontal sub-score bar (0-100), colored by tier, with optional weight.
 * @param {{ label: string, value?: number|null, weight?: number, compact?: boolean }} props
 */
export default function ScoreBar({ label, value = null, weight, compact = false }) {
  const pct = clamp(value ?? 0, 0, 100);
  const tokens = scoreColor(value);

  return (
    <div className="w-full">
      <div className="mb-1 flex items-baseline justify-between gap-2">
        <span className={`${compact ? 'text-[11px]' : 'text-xs'} font-medium text-slate-300`}>
          {label}
          {weight ? <span className="ml-1 text-slate-500">·{weight}%</span> : null}
        </span>
        <span className={`font-mono ${compact ? 'text-[11px]' : 'text-xs'} font-semibold ${tokens.text}`}>
          {displayScore(value)}
        </span>
      </div>
      <div
        className={`w-full overflow-hidden rounded-full bg-slate-700/50 ${compact ? 'h-1.5' : 'h-2'}`}
        role="progressbar"
        aria-label={`Score ${label}`}
        aria-valuenow={displayScore(value) === '—' ? undefined : Math.round(pct)}
        aria-valuemin={0}
        aria-valuemax={100}
      >
        <div
          className="h-full rounded-full transition-all duration-500"
          style={{ width: `${pct}%`, backgroundColor: tokens.hex }}
        />
      </div>
    </div>
  );
}

ScoreBar.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.number,
  weight: PropTypes.number,
  compact: PropTypes.bool,
};
