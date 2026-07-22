import { useState } from 'react';
import PropTypes from 'prop-types';
import { History, Radar, X } from 'lucide-react';
import { useScan, useScans } from '../hooks/useScans';
import StatusPill from '../components/StatusPill';
import RunScanModal from '../components/RunScanModal';
import ThesisCard from '../components/ThesisCard';
import { Empty, ErrorState, Loading, SkeletonList } from '../components/States';
import { displayScore, formatDateTime, timeAgo } from '../lib/format';

function ScanDrawer({ id, onClose }) {
  const { data, isLoading, isError, error, refetch } = useScan(id);
  const scan = data?.scan;
  const theses = data?.theses || [];

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <aside
        role="dialog"
        aria-modal="true"
        aria-label="Détails du scan"
        className="flex h-full w-full max-w-2xl animate-fade-in flex-col overflow-y-auto border-l border-terminal-border bg-terminal-bg p-5 shadow-2xl"
      >
        <div className="flex items-start justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-slate-100">
              {scan?.label || `Scan n°${id}`}
            </h2>
            {scan ? (
              <p className="mt-1 flex items-center gap-2 text-xs text-slate-400">
                <StatusPill status={scan.status} />
                <span>Démarré {formatDateTime(scan.started_at)}</span>
              </p>
            ) : null}
          </div>
          <button type="button" className="btn-ghost !p-2" onClick={onClose} aria-label="Fermer les détails du scan">
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="mt-5 flex-1">
          {isLoading ? (
            <Loading label="Chargement du scan…" />
          ) : isError ? (
            <ErrorState error={error} onRetry={refetch} />
          ) : theses.length === 0 ? (
            <Empty title="Aucune thèse dans ce scan" message="Ce scan n'a produit aucune thèse classée." />
          ) : (
            <>
              <div className="mb-3 grid grid-cols-2 gap-3">
                <div className="card p-3">
                  <p className="text-[11px] uppercase text-slate-500">Actifs</p>
                  <p className="font-mono text-xl font-bold text-slate-100">{scan?.num_assets ?? theses.length}</p>
                </div>
                <div className="card p-3">
                  <p className="text-[11px] uppercase text-slate-500">Score moyen</p>
                  <p className="font-mono text-xl font-bold text-emerald-400">{displayScore(scan?.avg_score)}</p>
                </div>
              </div>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {theses.map((t, i) => (
                  <ThesisCard key={t.id ?? `${t.ticker}-${i}`} thesis={t} rank={i + 1} />
                ))}
              </div>
            </>
          )}
        </div>
      </aside>
    </div>
  );
}
ScanDrawer.propTypes = {
  id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  onClose: PropTypes.func.isRequired,
};

export default function Scans() {
  const { data, isLoading, isError, error, refetch } = useScans();
  const [selected, setSelected] = useState(null);
  const [scanOpen, setScanOpen] = useState(false);
  const scans = data || [];

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="flex items-center gap-2 text-xl font-bold text-slate-100 sm:text-2xl">
            <History className="h-5 w-5 text-emerald-400" aria-hidden="true" /> Historique des scans
          </h1>
          <p className="text-sm text-slate-400">Chaque analyse par lot et ses résultats.</p>
        </div>
        <button type="button" className="btn-primary" onClick={() => setScanOpen(true)}>
          <Radar className="h-4 w-4" aria-hidden="true" /> Lancer un scan
        </button>
      </div>

      {isLoading ? (
        <SkeletonList rows={5} />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : scans.length === 0 ? (
        <Empty
          title="Aucun scan pour l'instant"
          message="Lancez votre premier scan pour analyser un ensemble de tickers."
          icon={Radar}
          action={
            <button type="button" className="btn-primary" onClick={() => setScanOpen(true)}>
              <Radar className="h-4 w-4" aria-hidden="true" /> Lancer un scan
            </button>
          }
        />
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[560px] text-left text-sm">
              <caption className="sr-only">Historique des scans</caption>
              <thead>
                <tr className="border-b border-terminal-border text-xs uppercase tracking-wide text-slate-500">
                  <th scope="col" className="px-4 py-3 font-medium">Libellé</th>
                  <th scope="col" className="px-4 py-3 font-medium">Statut</th>
                  <th scope="col" className="px-4 py-3 font-medium">Actifs</th>
                  <th scope="col" className="px-4 py-3 font-medium">Score moyen</th>
                  <th scope="col" className="px-4 py-3 font-medium">Démarré</th>
                </tr>
              </thead>
              <tbody>
                {scans.map((s) => (
                  <tr
                    key={s.id}
                    tabIndex={0}
                    role="button"
                    aria-label={`Ouvrir le scan ${s.label || s.id}`}
                    onClick={() => setSelected(s.id)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        setSelected(s.id);
                      }
                    }}
                    className="cursor-pointer border-b border-terminal-border/60 transition-colors last:border-0 hover:bg-slate-800/40 focus-visible:bg-slate-800/60"
                  >
                    <td className="px-4 py-3 font-medium text-slate-100">{s.label || `Scan n°${s.id}`}</td>
                    <td className="px-4 py-3"><StatusPill status={s.status} /></td>
                    <td className="px-4 py-3 font-mono text-slate-300">{s.num_assets ?? '—'}</td>
                    <td className="px-4 py-3 font-mono text-emerald-400">{displayScore(s.avg_score)}</td>
                    <td className="px-4 py-3 text-slate-400">{timeAgo(s.started_at)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selected !== null ? <ScanDrawer id={selected} onClose={() => setSelected(null)} /> : null}
      <RunScanModal open={scanOpen} onClose={() => setScanOpen(false)} />
    </div>
  );
}
