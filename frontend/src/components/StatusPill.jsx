import PropTypes from 'prop-types';

/**
 * Scan status pill.
 * @param {{ status?: string }} props
 */
export default function StatusPill({ status }) {
  const s = (status || 'unknown').toLowerCase();
  const map = {
    completed: 'bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/25',
    complete: 'bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/25',
    done: 'bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/25',
    success: 'bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/25',
    running: 'bg-amber-500/10 text-amber-300 ring-1 ring-amber-500/25',
    pending: 'bg-amber-500/10 text-amber-300 ring-1 ring-amber-500/25',
    queued: 'bg-amber-500/10 text-amber-300 ring-1 ring-amber-500/25',
    failed: 'bg-rose-500/10 text-rose-300 ring-1 ring-rose-500/25',
    error: 'bg-rose-500/10 text-rose-300 ring-1 ring-rose-500/25',
  };
  const cls = map[s] || 'bg-slate-500/10 text-slate-300 ring-1 ring-slate-500/25';
  const pulsing = ['running', 'pending', 'queued'].includes(s);

  // French display labels (styling/logic still keyed off the raw status).
  const labels = {
    completed: 'terminé',
    complete: 'terminé',
    done: 'terminé',
    success: 'réussi',
    running: 'en cours',
    pending: 'en attente',
    queued: 'en file',
    failed: 'échoué',
    error: 'erreur',
    unknown: 'inconnu',
  };

  return (
    <span className={`pill ${cls}`}>
      <span
        className={`h-1.5 w-1.5 rounded-full bg-current ${pulsing ? 'animate-pulse-soft' : ''}`}
        aria-hidden="true"
      />
      {labels[s] || s}
    </span>
  );
}

StatusPill.propTypes = {
  status: PropTypes.string,
};
