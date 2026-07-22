import { useMemo, useState } from 'react';
import { ClipboardList, FileText } from 'lucide-react';
import { useReports } from '../hooks/useReports';
import TickerBadge from '../components/TickerBadge';
import { Empty, ErrorState, SkeletonList } from '../components/States';
import { roleLabel } from '../lib/labels';
import { displayScore, scoreColor, timeAgo } from '../lib/format';

export default function Reports() {
  const { data, isLoading, isError, error, refetch } = useReports(50);
  const [role, setRole] = useState('all');
  const reports = data || [];

  // Build role options dynamically from returned data so filters always match.
  const roles = useMemo(() => {
    const set = new Set();
    reports.forEach((r) => r.agent_role && set.add(r.agent_role));
    return ['all', ...Array.from(set).sort()];
  }, [reports]);

  const filtered = role === 'all' ? reports : reports.filter((r) => r.agent_role === role);

  return (
    <div className="space-y-5">
      <div>
        <h1 className="flex items-center gap-2 text-xl font-bold text-slate-100 sm:text-2xl">
          <ClipboardList className="h-5 w-5 text-emerald-400" aria-hidden="true" /> Rapports des agents
        </h1>
        <p className="text-sm text-slate-400">Dernières analyses produites par l'ensemble des agents.</p>
      </div>

      {/* Role filter */}
      <div className="flex flex-wrap gap-2" role="group" aria-label="Filtrer par rôle d'agent">
        {roles.map((r) => {
          const active = role === r;
          return (
            <button
              key={r}
              type="button"
              onClick={() => setRole(r)}
              aria-pressed={active}
              className={`pill transition-colors ${
                active
                  ? 'bg-emerald-500/15 text-emerald-300 ring-1 ring-emerald-500/30'
                  : 'bg-slate-800 text-slate-400 ring-1 ring-terminal-border hover:text-slate-200'
              }`}
            >
              {r === 'all' ? 'Tous' : roleLabel(r)}
            </button>
          );
        })}
      </div>

      {isLoading ? (
        <SkeletonList rows={6} />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : filtered.length === 0 ? (
        <Empty
          title="Aucun rapport"
          message={role === 'all' ? 'Lancez un scan pour générer des rapports d\'agents.' : `Aucun rapport « ${roleLabel(role)} » pour l'instant.`}
          icon={FileText}
        />
      ) : (
        <ul className="space-y-3">
          {filtered.map((r, i) => {
            const tokens = scoreColor(r.score);
            return (
              <li key={r.id ?? i} className="card card-hover animate-fade-in p-4">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <TickerBadge ticker={r.ticker} />
                    <span className="pill bg-slate-800 text-slate-300 ring-1 ring-terminal-border">
                      {roleLabel(r.agent_role)}
                    </span>
                  </div>
                  <span className={`shrink-0 font-mono text-sm font-bold ${tokens.text}`}>
                    {displayScore(r.score)}
                  </span>
                </div>
                <p className="mt-2 whitespace-pre-line text-sm leading-relaxed text-slate-400">
                  {r.summary || 'Aucun résumé fourni.'}
                </p>
                <p className="mt-2 text-[11px] text-slate-500">{timeAgo(r.created_at)}</p>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
