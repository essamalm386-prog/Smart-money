import { useState } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import { Compass, Layers, Loader2, Sparkles, TrendingUp } from 'lucide-react';
import { useUniverse } from '../hooks/useUniverse';
import { useCreateScan } from '../hooks/useScans';
import { ErrorState, SkeletonList } from '../components/States';

/**
 * Une carte de secteur / thématique avec bouton d'analyse.
 */
function UniverseCard({ item, kind, onAnalyze, pending }) {
  const isSector = kind === 'sector';
  const cta = isSector ? 'Analyser ce secteur' : 'Analyser cette thématique';
  const busyLabel = `Analyse de « ${item.label} » en cours`;
  return (
    <article className="card card-hover flex flex-col gap-3 p-4">
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <h3 className="truncate text-sm font-semibold text-slate-100">{item.label}</h3>
          <p className="mt-0.5 text-xs text-slate-500">
            {item.count} valeur{item.count > 1 ? 's' : ''}
          </p>
        </div>
        <span
          className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-emerald-500/10 text-emerald-400"
          aria-hidden="true"
        >
          {isSector ? <Layers className="h-4 w-4" /> : <Sparkles className="h-4 w-4" />}
        </span>
      </div>

      <button
        type="button"
        className="btn-primary mt-auto w-full justify-center"
        onClick={() => onAnalyze(item)}
        disabled={pending}
        aria-label={pending ? busyLabel : `${cta} : ${item.label}`}
      >
        {pending ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
            Analyse…
          </>
        ) : (
          <>
            <TrendingUp className="h-4 w-4" aria-hidden="true" />
            {cta}
          </>
        )}
      </button>
    </article>
  );
}
UniverseCard.propTypes = {
  item: PropTypes.object.isRequired,
  kind: PropTypes.oneOf(['sector', 'domain']).isRequired,
  onAnalyze: PropTypes.func.isRequired,
  pending: PropTypes.bool,
};

export default function Discover() {
  const { data, isLoading, isError, error, refetch } = useUniverse();
  const createScan = useCreateScan();
  const navigate = useNavigate();
  // Clé de l'élément en cours d'analyse (ex. "sector:technologie").
  const [activeKey, setActiveKey] = useState(null);

  const sectors = data?.sectors || [];
  const domains = data?.domains || [];

  const analyze = async (kind, item) => {
    const key = `${kind}:${item.key}`;
    setActiveKey(key);
    try {
      const body = kind === 'sector' ? { sector: item.key } : { domain: item.key };
      await createScan.mutateAsync(body);
      // Redirige vers l'historique des scans où le résultat apparaît en tête.
      navigate('/scans');
    } catch {
      /* erreur affichée ci-dessous via createScan.error */
    } finally {
      setActiveKey(null);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="flex items-center gap-2 text-xl font-bold text-slate-100 sm:text-2xl">
          <Compass className="h-5 w-5 text-emerald-400" aria-hidden="true" />
          Découvrir où investir
        </h1>
        <p className="text-sm text-slate-400">
          Choisissez un secteur d'activité ou une thématique et lancez une analyse pour révéler les
          meilleures opportunités.
        </p>
      </div>

      {createScan.isError ? (
        <p
          role="alert"
          className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300"
        >
          {createScan.error?.message || 'Échec du lancement de l’analyse.'}
        </p>
      ) : null}

      {isLoading ? (
        <SkeletonList rows={6} />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : (
        <>
          <section aria-labelledby="discover-sectors-title" className="space-y-3">
            <div className="flex items-center gap-2">
              <Layers className="h-4 w-4 text-emerald-400" aria-hidden="true" />
              <h2
                id="discover-sectors-title"
                className="text-sm font-semibold uppercase tracking-wide text-slate-300"
              >
                Secteurs d'activité
              </h2>
            </div>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {sectors.map((s) => (
                <UniverseCard
                  key={s.key}
                  item={s}
                  kind="sector"
                  onAnalyze={(item) => analyze('sector', item)}
                  pending={activeKey === `sector:${s.key}`}
                />
              ))}
            </div>
          </section>

          <section aria-labelledby="discover-domains-title" className="space-y-3">
            <div className="flex items-center gap-2">
              <Sparkles className="h-4 w-4 text-emerald-400" aria-hidden="true" />
              <h2
                id="discover-domains-title"
                className="text-sm font-semibold uppercase tracking-wide text-slate-300"
              >
                Thématiques d'investissement
              </h2>
            </div>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {domains.map((d) => (
                <UniverseCard
                  key={d.key}
                  item={d}
                  kind="domain"
                  onAnalyze={(item) => analyze('domain', item)}
                  pending={activeKey === `domain:${d.key}`}
                />
              ))}
            </div>
          </section>
        </>
      )}
    </div>
  );
}
