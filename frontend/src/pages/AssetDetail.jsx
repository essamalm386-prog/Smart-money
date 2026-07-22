import { useState } from 'react';
import PropTypes from 'prop-types';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, Check, Eye, FileText, Plus } from 'lucide-react';
import { useAsset } from '../hooks/useAssets';
import { useAddToWatchlist, useWatchlist } from '../hooks/useWatchlist';
import ConvictionGauge from '../components/ConvictionGauge';
import ScoreBar from '../components/ScoreBar';
import RecommendationBadge from '../components/RecommendationBadge';
import Sparkline from '../components/Sparkline';
import TickerBadge from '../components/TickerBadge';
import SourceBadge from '../components/SourceBadge';
import ForecastBadge from '../components/ForecastBadge';
import { Empty, ErrorState, Loading } from '../components/States';
import { roleLabel } from '../lib/labels';
import {
  SUBSCORE_META,
  displayScore,
  formatDateTime,
  formatMarketCap,
  formatPrice,
  scoreColor,
  timeAgo,
} from '../lib/format';

function AgentReportCard({ report }) {
  const tokens = scoreColor(report.score);
  return (
    <article className="card p-4">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="rounded-md bg-slate-800 p-1.5 text-slate-300" aria-hidden="true">
            <FileText className="h-4 w-4" />
          </span>
          <h3 className="text-sm font-semibold text-slate-100">
            {roleLabel(report.agent_role)}
          </h3>
        </div>
        <span className={`font-mono text-sm font-bold ${tokens.text}`}>{displayScore(report.score)}</span>
      </div>
      <p className="mt-2 whitespace-pre-line text-sm leading-relaxed text-slate-400">
        {report.summary || 'Aucun résumé fourni.'}
      </p>
      <p className="mt-3 text-[11px] text-slate-500">{timeAgo(report.created_at)}</p>
    </article>
  );
}
AgentReportCard.propTypes = {
  report: PropTypes.shape({
    agent_role: PropTypes.string,
    summary: PropTypes.string,
    score: PropTypes.number,
    created_at: PropTypes.string,
  }).isRequired,
};

export default function AssetDetail() {
  const { ticker } = useParams();
  const { data, isLoading, isError, error, refetch } = useAsset(ticker);
  const addWatch = useAddToWatchlist();
  const watchlist = useWatchlist();
  const [note, setNote] = useState('');

  if (isLoading) return <Loading label={`Chargement de ${ticker}…`} />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;
  if (!data || !data.asset) {
    return <Empty title="Actif introuvable" message={`Aucune donnée pour ${ticker}.`} />;
  }

  const { asset, latest_thesis: thesis, reports = [], score_history: history = [] } = data;
  const dataSource = asset.data_source ?? thesis?.data_source ?? null;
  const dataProvider = asset.data_provider ?? thesis?.data_provider ?? null;
  const alreadyWatched = (watchlist.data || []).some(
    (w) => (w.ticker || '').toUpperCase() === (asset.ticker || '').toUpperCase()
  );

  const handleWatch = () => {
    if (alreadyWatched) return;
    addWatch.mutate({ ticker: asset.ticker, note: note.trim() || undefined });
  };

  return (
    <div className="space-y-6">
      <Link to="/" className="inline-flex items-center gap-1 text-sm text-slate-400 hover:text-slate-200">
        <ArrowLeft className="h-4 w-4" aria-hidden="true" /> Retour au tableau de bord
      </Link>

      {/* Header */}
      <section className="card flex flex-col gap-5 p-5 sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-2">
          <SourceBadge source={dataSource} provider={dataProvider} />
          <div className="flex flex-wrap items-center gap-2">
            <TickerBadge ticker={asset.ticker} linkTo={false} />
            <h1 className="text-xl font-bold text-slate-100 sm:text-2xl">{asset.name || asset.ticker}</h1>
          </div>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-slate-400">
            {asset.sector ? <span>{asset.sector}</span> : null}
            {asset.industry ? <span className="text-slate-500">· {asset.industry}</span> : null}
            {asset.country ? <span className="text-slate-500">· {asset.country}</span> : null}
          </div>
          <div className="flex flex-wrap items-center gap-x-6 gap-y-1 pt-1">
            <div>
              <p className="text-[11px] uppercase tracking-wide text-slate-500">Dernier cours</p>
              <p className="font-mono text-lg font-semibold text-slate-100">
                {formatPrice(asset.last_price, asset.currency)}
              </p>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wide text-slate-500">Capitalisation</p>
              <p className="font-mono text-lg font-semibold text-slate-100">
                {formatMarketCap(asset.market_cap, asset.currency)}
              </p>
            </div>
            {thesis?.recommendation ? (
              <div>
                <p className="text-[11px] uppercase tracking-wide text-slate-500">Recommandation</p>
                <RecommendationBadge recommendation={thesis.recommendation} />
              </div>
            ) : null}
          </div>
        </div>
        <div className="flex flex-col items-center gap-3">
          <ConvictionGauge score={asset.conviction_score ?? thesis?.conviction_score} size="lg" />
          <p className="text-[11px] text-slate-500">Mis à jour {timeAgo(asset.updated_at)}</p>
        </div>
      </section>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Sub-scores + history */}
        <div className="space-y-6 lg:col-span-1">
          <section className="card p-5">
            <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-300">
              Détail du score
            </h2>
            {thesis ? (
              <div className="space-y-4">
                {SUBSCORE_META.map((m) => (
                  <ScoreBar key={m.key} label={m.label} value={thesis[m.key]} weight={m.weight} />
                ))}
              </div>
            ) : (
              <p className="text-sm text-slate-500">Aucune thèse pour l'instant. Lancez un scan pour cet actif.</p>
            )}
          </section>

          <ForecastBadge
            outlook={thesis?.outlook ?? asset.outlook}
            volatility={thesis?.volatility ?? asset.volatility}
            geoRisk={thesis?.geo_risk ?? asset.geo_risk}
            geoScore={thesis?.geopolitical_score ?? asset.geopolitical_score}
            className="border-emerald-500/20"
          />

          <section className="card p-5">
            <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-300">
              Historique du score
            </h2>
            <Sparkline points={history} width={280} height={64} />
          </section>

          {/* Watchlist */}
          <section className="card p-5">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-slate-300">
              <Eye className="h-4 w-4" aria-hidden="true" /> Liste de suivi
            </h2>
            {alreadyWatched ? (
              <p className="inline-flex items-center gap-2 text-sm text-emerald-400">
                <Check className="h-4 w-4" aria-hidden="true" /> Dans votre liste de suivi
              </p>
            ) : (
              <div className="space-y-2">
                <label htmlFor="watch-note" className="label">
                  Note (facultatif)
                </label>
                <input
                  id="watch-note"
                  className="input"
                  placeholder="Pourquoi c'est important…"
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                />
                <button
                  type="button"
                  className="btn-primary w-full"
                  onClick={handleWatch}
                  disabled={addWatch.isPending}
                >
                  <Plus className="h-4 w-4" aria-hidden="true" />
                  {addWatch.isPending ? 'Ajout…' : 'Ajouter à la liste de suivi'}
                </button>
                {addWatch.isError ? (
                  <p role="alert" className="text-xs text-rose-400">
                    {addWatch.error?.message || "Impossible d'ajouter à la liste de suivi."}
                  </p>
                ) : null}
              </div>
            )}
          </section>
        </div>

        {/* Agent reports */}
        <div className="space-y-4 lg:col-span-2">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-300">Rapports des agents</h2>
            {thesis?.created_at ? (
              <span className="text-xs text-slate-500">Thèse · {formatDateTime(thesis.created_at)}</span>
            ) : null}
          </div>
          {thesis?.summary ? (
            <section className="card border-emerald-500/20 bg-emerald-500/[0.04] p-4">
              <p className="text-sm leading-relaxed text-slate-300">{thesis.summary}</p>
            </section>
          ) : null}
          {reports.length === 0 ? (
            <Empty title="Aucun rapport d'agent" message="Les rapports apparaissent après qu'un scan a analysé cet actif." icon={FileText} />
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              {reports.map((r, i) => (
                <AgentReportCard key={`${r.agent_role}-${i}`} report={r} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
