import PropTypes from 'prop-types';

/**
 * KPI stat card for the dashboard header.
 * @param {{
 *   label: string,
 *   value: React.ReactNode,
 *   icon?: React.ElementType,
 *   hint?: string,
 *   accent?: 'emerald'|'amber'|'rose'|'slate',
 *   loading?: boolean
 * }} props
 */
export default function StatCard({ label, value, icon: Icon, hint, accent = 'slate', loading = false }) {
  const accentText = {
    emerald: 'text-emerald-400',
    amber: 'text-amber-400',
    rose: 'text-rose-400',
    slate: 'text-slate-300',
  }[accent];

  return (
    <div className="card card-hover flex items-start justify-between gap-3 p-4">
      <div className="min-w-0">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-400">{label}</p>
        {loading ? (
          <div className="mt-2 h-7 w-20 animate-pulse-soft rounded bg-slate-700/60" />
        ) : (
          <p className="kpi-num mt-1 truncate">{value}</p>
        )}
        {hint ? <p className="mt-1 text-xs text-slate-500">{hint}</p> : null}
      </div>
      {Icon ? (
        <div className={`rounded-lg bg-slate-800/60 p-2 ${accentText}`} aria-hidden="true">
          <Icon className="h-5 w-5" />
        </div>
      ) : null}
    </div>
  );
}

StatCard.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.node,
  icon: PropTypes.elementType,
  hint: PropTypes.string,
  accent: PropTypes.oneOf(['emerald', 'amber', 'rose', 'slate']),
  loading: PropTypes.bool,
};
