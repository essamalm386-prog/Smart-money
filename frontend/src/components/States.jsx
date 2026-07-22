import PropTypes from 'prop-types';
import { AlertTriangle, Inbox, Loader2, RefreshCw } from 'lucide-react';

/**
 * Inline spinner / loading block.
 * @param {{ label?: string, className?: string }} props
 */
export function Loading({ label = 'Chargement…', className = '' }) {
  return (
    <div
      role="status"
      aria-live="polite"
      className={`flex flex-col items-center justify-center gap-3 py-12 text-slate-400 ${className}`}
    >
      <Loader2 className="h-6 w-6 animate-spin text-emerald-400" aria-hidden="true" />
      <span className="text-sm">{label}</span>
    </div>
  );
}
Loading.propTypes = {
  label: PropTypes.string,
  className: PropTypes.string,
};

/**
 * Skeleton placeholder rows.
 * @param {{ rows?: number, className?: string }} props
 */
export function SkeletonList({ rows = 4, className = '' }) {
  return (
    <div className={`space-y-3 ${className}`} aria-hidden="true">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="card h-20 animate-pulse-soft bg-slate-800/40" />
      ))}
    </div>
  );
}
SkeletonList.propTypes = {
  rows: PropTypes.number,
  className: PropTypes.string,
};

/**
 * Friendly empty state.
 * @param {{ title?: string, message?: string, icon?: React.ElementType, action?: React.ReactNode }} props
 */
export function Empty({ title = 'Rien à afficher pour l\'instant', message, icon: Icon = Inbox, action }) {
  return (
    <div className="card flex flex-col items-center justify-center gap-3 px-6 py-12 text-center">
      <Icon className="h-8 w-8 text-slate-500" aria-hidden="true" />
      <div>
        <p className="font-semibold text-slate-200">{title}</p>
        {message ? <p className="mt-1 text-sm text-slate-400">{message}</p> : null}
      </div>
      {action}
    </div>
  );
}
Empty.propTypes = {
  title: PropTypes.string,
  message: PropTypes.string,
  icon: PropTypes.elementType,
  action: PropTypes.node,
};

/**
 * Friendly error state with optional retry.
 * @param {{ error?: unknown, onRetry?: () => void, title?: string }} props
 */
export function ErrorState({ error, onRetry, title = 'Une erreur est survenue' }) {
  const message =
    (error && (error.message || String(error))) || 'Le serveur est injoignable.';
  return (
    <div
      role="alert"
      className="card flex flex-col items-center justify-center gap-3 border-rose-500/30 bg-rose-500/5 px-6 py-12 text-center"
    >
      <AlertTriangle className="h-8 w-8 text-rose-400" aria-hidden="true" />
      <div>
        <p className="font-semibold text-rose-200">{title}</p>
        <p className="mt-1 max-w-md text-sm text-rose-300/80">{message}</p>
      </div>
      {onRetry ? (
        <button type="button" className="btn-ghost" onClick={onRetry}>
          <RefreshCw className="h-4 w-4" aria-hidden="true" />
          Réessayer
        </button>
      ) : null}
    </div>
  );
}
ErrorState.propTypes = {
  error: PropTypes.oneOfType([PropTypes.object, PropTypes.string]),
  onRetry: PropTypes.func,
  title: PropTypes.string,
};
