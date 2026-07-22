import { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { Loader2, Radar, X } from 'lucide-react';
import { useCreateScan } from '../hooks/useScans';
import { useUniverse } from '../hooks/useUniverse';

/** Modes de scan disponibles. */
const MODES = [
  { key: 'tickers', label: 'Par symboles' },
  { key: 'sector', label: 'Par secteur' },
  { key: 'domain', label: 'Par thématique' },
];

/**
 * Parse a free-form ticker string into a clean, de-duplicated uppercase list.
 * @param {string} raw
 * @returns {string[]}
 */
export function parseTickers(raw) {
  return Array.from(
    new Set(
      (raw || '')
        .split(/[\s,;\n]+/)
        .map((t) => t.trim().toUpperCase())
        .filter(Boolean)
    )
  );
}

/**
 * Modal to launch a new scan (POST /scans).
 * @param {{ open: boolean, onClose: () => void, onLaunched?: (scan:object) => void }} props
 */
export default function RunScanModal({ open, onClose, onLaunched }) {
  const [mode, setMode] = useState('tickers');
  const [tickersRaw, setTickersRaw] = useState('');
  const [label, setLabel] = useState('');
  const [sector, setSector] = useState('');
  const [domain, setDomain] = useState('');
  const createScan = useCreateScan();
  const universe = useUniverse();
  const inputRef = useRef(null);
  const dialogRef = useRef(null);

  const tickers = parseTickers(tickersRaw);
  const sectors = universe.data?.sectors || [];
  const domains = universe.data?.domains || [];

  // Un scan est prêt selon le mode actif.
  const canSubmit =
    (mode === 'tickers' && tickers.length > 0) ||
    (mode === 'sector' && Boolean(sector)) ||
    (mode === 'domain' && Boolean(domain));

  useEffect(() => {
    if (open) {
      setTimeout(() => inputRef.current?.focus(), 50);
    } else {
      setMode('tickers');
      setTickersRaw('');
      setLabel('');
      setSector('');
      setDomain('');
      createScan.reset();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canSubmit) return;
    const body = { label: label.trim() || undefined };
    if (mode === 'tickers') body.tickers = tickers;
    else if (mode === 'sector') body.sector = sector;
    else if (mode === 'domain') body.domain = domain;
    try {
      const scan = await createScan.mutateAsync(body);
      onLaunched?.(scan);
      onClose();
    } catch {
      /* error surfaced below via createScan.error */
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-black/70 p-0 backdrop-blur-sm sm:items-center sm:p-4"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="run-scan-title"
        className="card animate-fade-in w-full max-w-lg rounded-b-none rounded-t-2xl p-5 sm:rounded-2xl"
      >
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2">
            <span className="rounded-lg bg-emerald-500/10 p-2 text-emerald-400" aria-hidden="true">
              <Radar className="h-5 w-5" />
            </span>
            <div>
              <h2 id="run-scan-title" className="text-lg font-semibold text-slate-100">
                Lancer un scan
              </h2>
              <p className="text-xs text-slate-400">Analysez des tickers et générez des thèses de conviction.</p>
            </div>
          </div>
          <button type="button" className="btn-ghost !p-2" onClick={onClose} aria-label="Fermer la fenêtre">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          {/* Choix du mode de scan */}
          <div>
            <span className="label">Mode d'analyse</span>
            <div
              className="mt-1 grid grid-cols-3 gap-1 rounded-lg border border-terminal-border bg-terminal-panel/60 p-1"
              role="tablist"
              aria-label="Mode d'analyse"
            >
              {MODES.map((m) => (
                <button
                  key={m.key}
                  type="button"
                  role="tab"
                  aria-selected={mode === m.key}
                  className={`rounded-md px-2 py-1.5 text-xs font-medium transition-colors ${
                    mode === m.key
                      ? 'bg-emerald-500/15 text-emerald-300 ring-1 ring-emerald-500/25'
                      : 'text-slate-400 hover:text-slate-100'
                  }`}
                  onClick={() => setMode(m.key)}
                >
                  {m.label}
                </button>
              ))}
            </div>
          </div>

          {mode === 'tickers' ? (
            <div>
              <label htmlFor="scan-tickers" className="label">
                Tickers
              </label>
              <textarea
                id="scan-tickers"
                ref={inputRef}
                className="input min-h-[84px] resize-y font-mono uppercase"
                placeholder="AAPL, MSFT, NVDA"
                value={tickersRaw}
                onChange={(e) => setTickersRaw(e.target.value)}
                aria-describedby="scan-tickers-hint"
              />
              <p id="scan-tickers-hint" className="mt-1 text-xs text-slate-500">
                Séparez par des virgules, des espaces ou des retours à la ligne.
                {tickers.length > 0 ? (
                  <span className="ml-1 text-emerald-400">
                    {tickers.length} ticker{tickers.length > 1 ? 's' : ''} prêt{tickers.length > 1 ? 's' : ''}.
                  </span>
                ) : null}
              </p>
            </div>
          ) : null}

          {mode === 'sector' ? (
            <div>
              <label htmlFor="scan-sector" className="label">
                Secteur d'activité
              </label>
              <select
                id="scan-sector"
                className="input"
                value={sector}
                onChange={(e) => setSector(e.target.value)}
              >
                <option value="">Choisir un secteur…</option>
                {sectors.map((s) => (
                  <option key={s.key} value={s.key}>
                    {s.label} ({s.count})
                  </option>
                ))}
              </select>
            </div>
          ) : null}

          {mode === 'domain' ? (
            <div>
              <label htmlFor="scan-domain" className="label">
                Thématique d'investissement
              </label>
              <select
                id="scan-domain"
                className="input"
                value={domain}
                onChange={(e) => setDomain(e.target.value)}
              >
                <option value="">Choisir une thématique…</option>
                {domains.map((d) => (
                  <option key={d.key} value={d.key}>
                    {d.label} ({d.count})
                  </option>
                ))}
              </select>
            </div>
          ) : null}

          <div>
            <label htmlFor="scan-label" className="label">
              Libellé <span className="text-slate-600">(facultatif)</span>
            </label>
            <input
              id="scan-label"
              className="input"
              placeholder="Revue hebdomadaire des mégacapitalisations"
              value={label}
              onChange={(e) => setLabel(e.target.value)}
            />
          </div>

          {createScan.isError ? (
            <p role="alert" className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
              {createScan.error?.message || 'Échec du lancement du scan.'}
            </p>
          ) : null}

          <div className="flex items-center justify-end gap-2 pt-1">
            <button type="button" className="btn-ghost" onClick={onClose}>
              Annuler
            </button>
            <button type="submit" className="btn-primary" disabled={!canSubmit || createScan.isPending}>
              {createScan.isPending ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
                  Lancement…
                </>
              ) : (
                <>
                  <Radar className="h-4 w-4" aria-hidden="true" />
                  Lancer un scan
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

RunScanModal.propTypes = {
  open: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onLaunched: PropTypes.func,
};
