import { Link } from 'react-router-dom';
import { Eye, Trash2 } from 'lucide-react';
import { useRemoveFromWatchlist, useWatchlist } from '../hooks/useWatchlist';
import ConvictionGauge from '../components/ConvictionGauge';
import TickerBadge from '../components/TickerBadge';
import { Empty, ErrorState, SkeletonList } from '../components/States';
import { timeAgo } from '../lib/format';

export default function Watchlist() {
  const { data, isLoading, isError, error, refetch } = useWatchlist();
  const remove = useRemoveFromWatchlist();
  const items = data || [];

  return (
    <div className="space-y-5">
      <div>
        <h1 className="flex items-center gap-2 text-xl font-bold text-slate-100 sm:text-2xl">
          <Eye className="h-5 w-5 text-emerald-400" aria-hidden="true" /> Liste de suivi
        </h1>
        <p className="text-sm text-slate-400">Les tickers que vous suivez, avec vos notes.</p>
      </div>

      {isLoading ? (
        <SkeletonList rows={4} />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : items.length === 0 ? (
        <Empty
          title="Votre liste de suivi est vide"
          message="Ajoutez des tickers depuis n'importe quelle page de détail d'actif pour les suivre ici."
          icon={Eye}
          action={
            <Link to="/" className="btn-primary">
              Parcourir les opportunités
            </Link>
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((item) => (
            <article key={item.id ?? item.ticker} className="card card-hover flex flex-col gap-3 p-4">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <TickerBadge ticker={item.ticker} />
                  <Link
                    to={`/assets/${encodeURIComponent(item.ticker || '')}`}
                    className="mt-2 block truncate text-sm font-semibold text-slate-100 hover:text-emerald-300"
                  >
                    {item.name || item.ticker}
                  </Link>
                  <p className="mt-0.5 text-[11px] text-slate-500">Ajouté {timeAgo(item.created_at)}</p>
                </div>
                <ConvictionGauge score={item.conviction_score} size="sm" label={false} />
              </div>

              {item.note ? (
                <p className="rounded-lg bg-slate-800/50 px-3 py-2 text-xs leading-relaxed text-slate-300">
                  {item.note}
                </p>
              ) : (
                <p className="text-xs italic text-slate-500">Aucune note.</p>
              )}

              <div className="mt-auto flex justify-end">
                <button
                  type="button"
                  className="btn-danger !py-1.5 text-xs"
                  onClick={() => remove.mutate(item.ticker)}
                  disabled={remove.isPending && remove.variables === item.ticker}
                  aria-label={`Retirer ${item.ticker} de la liste de suivi`}
                >
                  <Trash2 className="h-3.5 w-3.5" aria-hidden="true" /> Retirer
                </button>
              </div>
            </article>
          ))}
        </div>
      )}

      {remove.isError ? (
        <p role="alert" className="text-sm text-rose-400">
          {remove.error?.message || "Impossible de retirer l'élément."}
        </p>
      ) : null}
    </div>
  );
}
