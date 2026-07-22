import { Component } from 'react';
import PropTypes from 'prop-types';
import { resetToOffline, hardReset } from '../services/recovery';

/**
 * Global safety net. If anything throws during render, the user still sees a
 * clear French recovery screen with buttons to get unstuck — the app can never
 * become a dead "error only" screen again.
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, message: '' };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, message: error?.message || 'Erreur inconnue' };
  }

  componentDidCatch(error, info) {
    // eslint-disable-next-line no-console
    console.error('Smart Money — erreur capturée :', error, info);
  }

  render() {
    if (!this.state.hasError) return this.props.children;
    return (
      <div className="flex min-h-screen items-center justify-center bg-terminal-bg p-6 text-slate-200">
        <div className="w-full max-w-md rounded-2xl border border-terminal-border bg-terminal-panel p-6 text-center">
          <h1 className="mb-2 text-lg font-semibold text-slate-100">Une erreur est survenue</h1>
          <p className="mb-1 text-sm text-slate-400">
            L'application a rencontré un problème. Vous pouvez la remettre en état
            immédiatement :
          </p>
          <p className="mb-5 text-xs text-slate-500 break-words">{this.state.message}</p>
          <div className="space-y-2">
            <button
              type="button"
              className="btn-primary w-full justify-center"
              onClick={() => resetToOffline()}
            >
              Revenir en mode hors-ligne
            </button>
            <button
              type="button"
              className="btn-ghost w-full justify-center"
              onClick={() => window.location.assign(window.location.pathname)}
            >
              Recharger l'application
            </button>
            <button
              type="button"
              className="w-full justify-center rounded-lg px-3 py-2 text-sm font-medium text-rose-300 hover:bg-rose-500/10"
              onClick={() => hardReset()}
            >
              Tout réinitialiser (efface les réglages et les données locales)
            </button>
          </div>
        </div>
      </div>
    );
  }
}

ErrorBoundary.propTypes = {
  children: PropTypes.node,
};
