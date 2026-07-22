import { useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { Award, BarChart3, Clock, FlaskConical, Filter, Gauge, Radar, ShieldCheck, TrendingUp } from 'lucide-react';
import { useTheses } from '../hooks/useTheses';
import { useAssets } from '../hooks/useAssets';
import { useScans } from '../hooks/useScans';
import StatCard from '../components/StatCard';
import ThesisCard from '../components/ThesisCard';
import RunScanModal from '../components/RunScanModal';
import { Empty, ErrorState, SkeletonList } from '../components/States';
import { displayScore, timeAgo } from '../lib/format';

/** One-line banner describing whether the dashboard shows real or simulated data. */
function DataModeBanner({ theses }) {
  const sample = theses.find((t) => t && t.data_source) || null;
  const source = sample?.data_source ? String(sample.data_source).toLowerCase() : null;
  if (!source) return null;

  if (source === 'real') {
    const provider = sample?.data_provider;
    return (
      <div
        role="status"
        className="flex items-start gap-2 rounded-lg border border-emerald-500/25 bg-emerald-500/[0.06] px-4 py-3 text-sm text-emerald-200"
      >
        <ShieldCheck className="mt-0.5 h-4 w-4 shrink-0 text-emerald-400" aria-hidden="true" />
        <p>
          Mode serveur : les données affichées sont <strong>réelles</strong>
          {provider ? ` et fournies par ${provider}.` : ' et à jour.'}
        </p>
      </div>
    );
  }

  return (
    <div
      role="status"
      className="flex items-start gap-2 rounded-lg border border-amber-500/25 bg-amber-500/[0.06] px-4 py-3 text-sm text-amber-200"
    >
      <FlaskConical className="mt-0.5 h-4 w-4 shrink-0 text-amber-400" aria-hidden="true" />
      <p>
        Mode hors ligne : les données affichées sont <strong>simulées</strong> (générées sur
        l'appareil pour tester l'interface). Pour des données réelles, connectez-vous à un serveur
        dans la Configuration.
      </p>
    </div>
  );
}

DataModeBanner.propTypes = {
  theses: PropTypes.array.isRequired,
};

export default function Dashboard() {
  const theses = useTheses(50);
  const assets = useAssets();
  const scans = useScans();
  const [scanOpen, setScanOpen] = useState(false);
  const [sectorFilter, setSectorFilter] = useState('all');

  // Associe chaque ticker à son secteur (les thèses ne portent pas toujours le
  // champ sector, on le récupère depuis les actifs).
  const sectorByTicker = useMemo(() => {
    const map = new Map();
    (assets.data || []).forEach((a) => {
      if (a?.ticker && a?.sector) map.set(a.ticker, a.sector);
    });
    return map;
  }, [assets.data]);

  const sectorOf = (t) => t?.sector || sectorByTicker.get(t?.ticker) || null;

  // Secteurs réellement présents dans les opportunités affichées.
  const availableSectors = useMemo(() => {
    const set = new Set();
    (theses.data || []).forEach((t) => {
      const s = sectorOf(t);
      if (s) set.add(s);
    });
    return Array.from(set).sort((a, b) => a.localeCompare(b, 'fr'));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [theses.data, sectorByTicker]);

  const kpis = useMemo(() => {
    const list = theses.data || [];
    const assetList = assets.data || [];
    const scanList = scans.data || [];
    const avg =
      list.length > 0
        ? list.reduce((sum, t) => sum + (Number(t.conviction_score) || 0), 0) / list.length
        : null;
    const top = list[0] || null;
    const lastScan = scanList
      .slice()
      .sort((a, b) => new Date(b.started_at || 0) - new Date(a.started_at || 0))[0];
    return {
      assetsScanned: assetList.length,
      avgConviction: avg,
      top,
      lastScanTime: lastScan?.started_at || null,
    };
  }, [theses.data, assets.data, scans.data]);

  const allTheses = theses.data || [];
  const list =
    sectorFilter === 'all'
      ? allTheses
      : allTheses.filter((t) => sectorOf(t) === sectorFilter);

  // Réinitialise le filtre s'il n'est plus disponible après un nouveau scan.
  if (sectorFilter !== 'all' && availableSectors.length && !availableSectors.includes(sectorFilter)) {
    setSectorFilter('all');
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-slate-100 sm:text-2xl">Tableau de bord des opportunités</h1>
          <p className="text-sm text-slate-400">Thèses d'investissement classées par conviction.</p>
        </div>
        <button type="button" className="btn-primary" onClick={() => setScanOpen(true)}>
          <Radar className="h-4 w-4" aria-hidden="true" />
          Lancer un scan
        </button>
      </div>

      {/* Data-mode banner */}
      <DataModeBanner theses={list} />

      {/* KPI row */}
      <section aria-label="Indicateurs clés" className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard
          label="Actifs analysés"
          value={kpis.assetsScanned}
          icon={BarChart3}
          accent="slate"
          loading={assets.isLoading}
        />
        <StatCard
          label="Conviction moyenne"
          value={displayScore(kpis.avgConviction)}
          icon={Gauge}
          accent="emerald"
          loading={theses.isLoading}
        />
        <StatCard
          label="Meilleure opportunité"
          value={kpis.top ? (kpis.top.ticker || '—') : '—'}
          hint={kpis.top ? `conviction ${displayScore(kpis.top.conviction_score)}` : undefined}
          icon={Award}
          accent="amber"
          loading={theses.isLoading}
        />
        <StatCard
          label="Dernier scan"
          value={kpis.lastScanTime ? timeAgo(kpis.lastScanTime) : '—'}
          icon={Clock}
          accent="slate"
          loading={scans.isLoading}
        />
      </section>

      {/* Ranked theses */}
      <section aria-label="Meilleures thèses" className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-emerald-400" aria-hidden="true" />
            <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-300">Meilleures thèses</h2>
          </div>
          {availableSectors.length > 0 ? (
            <div className="flex items-center gap-2">
              <label htmlFor="sector-filter" className="flex items-center gap-1.5 text-xs text-slate-400">
                <Filter className="h-3.5 w-3.5" aria-hidden="true" />
                Filtrer par secteur
              </label>
              <select
                id="sector-filter"
                className="input !w-auto !py-1.5 text-sm"
                value={sectorFilter}
                onChange={(e) => setSectorFilter(e.target.value)}
              >
                <option value="all">Tous les secteurs</option>
                {availableSectors.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
          ) : null}
        </div>

        {theses.isLoading ? (
          <SkeletonList rows={6} />
        ) : theses.isError ? (
          <ErrorState error={theses.error} onRetry={theses.refetch} />
        ) : list.length === 0 ? (
          <Empty
            title="Aucune thèse pour l'instant"
            message="Lancez un scan pour générer des thèses d'investissement classées par conviction."
            icon={Radar}
            action={
              <button type="button" className="btn-primary" onClick={() => setScanOpen(true)}>
                <Radar className="h-4 w-4" aria-hidden="true" />
                Lancer votre premier scan
              </button>
            }
          />
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {list.map((t, i) => (
              <ThesisCard key={t.id ?? `${t.ticker}-${i}`} thesis={t} rank={i + 1} />
            ))}
          </div>
        )}
      </section>

      <RunScanModal open={scanOpen} onClose={() => setScanOpen(false)} />
    </div>
  );
}
