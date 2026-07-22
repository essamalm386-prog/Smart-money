import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { AlertTriangle, Building2, Check, Cloud, HardDrive, KeyRound, LifeBuoy, RotateCcw, Save, Settings, Sparkles, Trash2 } from 'lucide-react';
import { useConfig, useUpdateConfig } from '../hooks/useConfig';
import { usePappersStatus } from '../hooks/usePappers';
import * as dataProvider from '../services/dataProvider';
import { resetToOffline, hardReset } from '../services/recovery';
import { ErrorState, Loading } from '../components/States';

/** Convert a comma/newline list string into a clean array. */
function toList(str) {
  return (str || '')
    .split(/[\n,]+/)
    .map((s) => s.trim())
    .filter(Boolean);
}

export default function Config() {
  const qc = useQueryClient();
  const { data, isLoading, isError, error, refetch } = useConfig();
  const update = useUpdateConfig();
  const pappersStatus = usePappersStatus();

  // Client-side data-source settings (not part of the backend config payload).
  const [mode, setModeState] = useState(dataProvider.getMode());
  const [serverUrl, setServerUrlState] = useState(dataProvider.getServerUrl());
  const [sourceSaved, setSourceSaved] = useState(false);

  const applyDataSource = (nextMode) => {
    dataProvider.setMode(nextMode);
    dataProvider.setServerUrl(serverUrl);
    setModeState(nextMode);
    setSourceSaved(true);
    // All cached queries were served by the previous source — refetch fresh.
    qc.invalidateQueries();
    setTimeout(() => setSourceSaved(false), 2500);
  };

  const [form, setForm] = useState({
    min_market_cap: '',
    max_pe: '',
    excluded_sectors: '',
    scan_universe: '',
    use_llm: false,
  });

  useEffect(() => {
    if (!data) return;
    setForm({
      min_market_cap: data.min_market_cap ?? '',
      max_pe: data.max_pe ?? '',
      excluded_sectors: Array.isArray(data.excluded_sectors)
        ? data.excluded_sectors.join(', ')
        : data.excluded_sectors || '',
      scan_universe: Array.isArray(data.scan_universe)
        ? data.scan_universe.join(', ')
        : data.scan_universe || '',
      use_llm: Boolean(data.use_llm),
    });
  }, [data]);

  if (isLoading) return <Loading label="Chargement de la configuration…" />;
  if (isError) return <ErrorState error={error} onRetry={refetch} />;

  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = {
      min_market_cap: form.min_market_cap === '' ? null : Number(form.min_market_cap),
      max_pe: form.max_pe === '' ? null : Number(form.max_pe),
      excluded_sectors: toList(form.excluded_sectors),
      scan_universe: toList(form.scan_universe),
      use_llm: form.use_llm,
    };
    update.mutate(payload);
  };

  const anthropicConfigured = Boolean(data?.anthropic_configured);
  const pappersConfigured =
    Boolean(data?.pappers_configured) || Boolean(pappersStatus.data?.configured);

  return (
    <div className="mx-auto max-w-2xl space-y-5">
      <div>
        <h1 className="flex items-center gap-2 text-xl font-bold text-slate-100 sm:text-2xl">
          <Settings className="h-5 w-5 text-emerald-400" aria-hidden="true" /> Configuration
        </h1>
        <p className="text-sm text-slate-400">Réglez l'univers de scan et le moteur d'analyse.</p>
      </div>

      {/* Data source */}
      <section className="card space-y-4 p-5">
        <div className="flex items-center gap-2">
          <span className={`rounded-lg p-2 ${mode === 'local' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-sky-500/10 text-sky-400'}`}>
            {mode === 'local' ? <HardDrive className="h-5 w-5" aria-hidden="true" /> : <Cloud className="h-5 w-5" aria-hidden="true" />}
          </span>
          <div>
            <h2 className="text-sm font-semibold text-slate-100">Source de données</h2>
            <p className="text-xs text-slate-400">
              Active :{' '}
              <span className={mode === 'local' ? 'text-emerald-400' : 'text-sky-400'}>
                {mode === 'local' ? "Sur l'appareil (hors-ligne)" : 'Connecté au serveur'}
              </span>
            </p>
          </div>
        </div>

        {/* Data-source honesty warning */}
        <div
          className={`flex items-start gap-2 rounded-lg border px-3 py-2.5 text-xs ${
            mode === 'local'
              ? 'border-amber-500/25 bg-amber-500/[0.06] text-amber-200'
              : 'border-sky-500/25 bg-sky-500/[0.06] text-sky-200'
          }`}
        >
          <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
          <p>
            En mode « Sur l'appareil (hors-ligne) », les données sont <strong>simulées</strong> (pour
            tester l'interface). Pour des données <strong>réelles</strong> et à jour, choisissez « Se
            connecter au serveur » et renseignez l'URL du serveur ci-dessous.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2" role="group" aria-label="Choisir la source de données">
          <button
            type="button"
            aria-pressed={mode === 'local'}
            onClick={() => applyDataSource('local')}
            className={`flex items-start gap-3 rounded-lg border px-4 py-3 text-left transition-colors ${
              mode === 'local'
                ? 'border-emerald-500/40 bg-emerald-500/[0.06]'
                : 'border-terminal-border bg-terminal-bg hover:border-slate-600'
            }`}
          >
            <HardDrive className="mt-0.5 h-5 w-5 shrink-0 text-emerald-400" aria-hidden="true" />
            <span>
              <span className="block text-sm font-medium text-slate-100">Sur l'appareil (hors-ligne)</span>
              <span className="block text-xs text-slate-500">Exécute le moteur d'analyse entièrement dans votre navigateur. Aucun serveur requis. Données simulées.</span>
            </span>
          </button>

          <button
            type="button"
            aria-pressed={mode === 'remote'}
            onClick={() => applyDataSource('remote')}
            className={`flex items-start gap-3 rounded-lg border px-4 py-3 text-left transition-colors ${
              mode === 'remote'
                ? 'border-sky-500/40 bg-sky-500/[0.06]'
                : 'border-terminal-border bg-terminal-bg hover:border-slate-600'
            }`}
          >
            <Cloud className="mt-0.5 h-5 w-5 shrink-0 text-sky-400" aria-hidden="true" />
            <span>
              <span className="block text-sm font-medium text-slate-100">Se connecter au serveur</span>
              <span className="block text-xs text-slate-500">Utilise un serveur Smart Money (FastAPI) en fonctionnement. Données réelles.</span>
            </span>
          </button>
        </div>

        <div>
          <label htmlFor="server_url" className="label">URL du serveur</label>
          <input
            id="server_url"
            type="url"
            inputMode="url"
            className="input font-mono"
            value={serverUrl}
            onChange={(e) => setServerUrlState(e.target.value)}
            onBlur={() => dataProvider.setServerUrl(serverUrl)}
            placeholder="https://votre-hote:8000/api"
            aria-describedby="server-url-hint"
          />
          <p id="server-url-hint" className="mt-1 text-xs text-slate-500">
            URL de base de l'API du serveur (incluez <span className="font-mono">/api</span>). Utilisée uniquement en mode serveur.
          </p>
        </div>

        <div className="flex items-center justify-between gap-3">
          <div aria-live="polite" className="text-sm">
            {sourceSaved ? (
              <span className="inline-flex items-center gap-1.5 text-emerald-400">
                <Check className="h-4 w-4" aria-hidden="true" /> Source de données mise à jour
              </span>
            ) : null}
          </div>
          {mode === 'remote' ? (
            <button
              type="button"
              className="btn-ghost"
              onClick={() => applyDataSource('remote')}
            >
              <Cloud className="h-4 w-4" aria-hidden="true" /> Appliquer l'URL du serveur
            </button>
          ) : null}
        </div>
      </section>

      {/* Engine status */}
      <section className="card grid grid-cols-1 gap-3 p-4 sm:grid-cols-2">
        <div className="flex items-center gap-3">
          <span className={`rounded-lg p-2 ${anthropicConfigured ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'}`}>
            <KeyRound className="h-5 w-5" aria-hidden="true" />
          </span>
          <div>
            <p className="text-sm font-medium text-slate-200">Clé Anthropic</p>
            <p className={`text-xs ${anthropicConfigured ? 'text-emerald-400' : 'text-rose-400'}`}>
              {anthropicConfigured ? 'Configurée' : 'Non configurée'}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <span className={`rounded-lg p-2 ${form.use_llm ? 'bg-emerald-500/10 text-emerald-400' : 'bg-slate-700/50 text-slate-400'}`}>
            <Sparkles className="h-5 w-5" aria-hidden="true" />
          </span>
          <div>
            <p className="text-sm font-medium text-slate-200">Mode LLM</p>
            <p className={`text-xs ${form.use_llm ? 'text-emerald-400' : 'text-slate-400'}`}>
              {form.use_llm ? 'Activé' : 'Désactivé'}
            </p>
          </div>
        </div>
      </section>

      {/* Source Pappers (entreprises françaises) */}
      <section className="card space-y-2 p-4">
        <div className="flex items-center gap-3">
          <span className={`rounded-lg p-2 ${pappersConfigured ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'}`}>
            <Building2 className="h-5 w-5" aria-hidden="true" />
          </span>
          <div>
            <p className="text-sm font-medium text-slate-200">Source Pappers (entreprises françaises)</p>
            <p className={`text-xs ${pappersConfigured ? 'text-emerald-400' : 'text-rose-400'}`}>
              {pappersConfigured ? 'Configurée' : 'Non configurée'}
            </p>
          </div>
        </div>
        <p className="text-xs text-slate-500">
          Pour l'activer, passez en mode « Se connecter au serveur » et ajoutez une clé
          PAPPERS_API_KEY (gratuite sur pappers.fr/api) dans les variables du serveur.
        </p>
      </section>

      <form onSubmit={handleSubmit} className="card space-y-5 p-5">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="min_market_cap" className="label">Capitalisation minimale</label>
            <input
              id="min_market_cap"
              type="number"
              min="0"
              step="any"
              className="input font-mono"
              value={form.min_market_cap}
              onChange={(e) => setForm((f) => ({ ...f, min_market_cap: e.target.value }))}
              placeholder="e.g. 2000000000"
            />
          </div>
          <div>
            <label htmlFor="max_pe" className="label">P/E maximal</label>
            <input
              id="max_pe"
              type="number"
              min="0"
              step="any"
              className="input font-mono"
              value={form.max_pe}
              onChange={(e) => setForm((f) => ({ ...f, max_pe: e.target.value }))}
              placeholder="e.g. 40"
            />
          </div>
        </div>

        <div>
          <label htmlFor="excluded_sectors" className="label">Secteurs exclus</label>
          <textarea
            id="excluded_sectors"
            className="input min-h-[64px] resize-y"
            value={form.excluded_sectors}
            onChange={(e) => setForm((f) => ({ ...f, excluded_sectors: e.target.value }))}
            placeholder="Tabac, Jeux d'argent"
            aria-describedby="excluded-hint"
          />
          <p id="excluded-hint" className="mt-1 text-xs text-slate-500">Séparés par des virgules ou des retours à la ligne.</p>
        </div>

        <div>
          <label htmlFor="scan_universe" className="label">Univers de scan</label>
          <textarea
            id="scan_universe"
            className="input min-h-[64px] resize-y font-mono uppercase"
            value={form.scan_universe}
            onChange={(e) => setForm((f) => ({ ...f, scan_universe: e.target.value }))}
            placeholder="AAPL, MSFT, NVDA"
            aria-describedby="universe-hint"
          />
          <p id="universe-hint" className="mt-1 text-xs text-slate-500">Tickers considérés par défaut lors des scans.</p>
        </div>

        <label className="flex cursor-pointer items-center justify-between rounded-lg border border-terminal-border bg-terminal-bg px-3 py-3">
          <span className="text-sm font-medium text-slate-200">
            Utiliser l'analyse LLM
            <span className="block text-xs font-normal text-slate-500">Nécessite une clé Anthropic configurée.</span>
          </span>
          <input
            type="checkbox"
            className="h-5 w-5 accent-emerald-500"
            checked={form.use_llm}
            onChange={(e) => setForm((f) => ({ ...f, use_llm: e.target.checked }))}
            aria-label="Utiliser l'analyse LLM"
          />
        </label>

        <div className="flex items-center justify-between gap-3 pt-1">
          <div aria-live="polite" className="text-sm">
            {update.isSuccess ? (
              <span className="inline-flex items-center gap-1.5 text-emerald-400">
                <Check className="h-4 w-4" aria-hidden="true" /> Enregistré
              </span>
            ) : update.isError ? (
              <span role="alert" className="text-rose-400">{update.error?.message || "Échec de l'enregistrement."}</span>
            ) : null}
          </div>
          <button type="submit" className="btn-primary" disabled={update.isPending}>
            <Save className="h-4 w-4" aria-hidden="true" />
            {update.isPending ? 'Enregistrement…' : 'Enregistrer les modifications'}
          </button>
        </div>
      </form>

      {/* Dépannage / récupération */}
      <section className="card space-y-4 p-5">
        <div className="flex items-center gap-2">
          <span className="rounded-lg bg-amber-500/10 p-2 text-amber-400">
            <LifeBuoy className="h-5 w-5" aria-hidden="true" />
          </span>
          <div>
            <h2 className="text-sm font-semibold text-slate-100">Dépannage</h2>
            <p className="text-xs text-slate-400">
              À utiliser si l'application se bloque après un mauvais réglage (par exemple une URL
              de serveur incorrecte).
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <button
            type="button"
            className="btn-ghost justify-start"
            onClick={() => resetToOffline()}
          >
            <RotateCcw className="h-4 w-4" aria-hidden="true" /> Revenir en mode hors-ligne
          </button>
          <button
            type="button"
            className="inline-flex items-center justify-start gap-2 rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-2 text-sm font-medium text-rose-300 transition-colors hover:bg-rose-500/20"
            onClick={() => {
              if (
                window.confirm(
                  'Tout réinitialiser ? Cette action efface tous vos réglages et données locales '
                    + '(scans, thèses, liste de suivi) et recharge l’application à neuf.'
                )
              ) {
                hardReset();
              }
            }}
          >
            <Trash2 className="h-4 w-4" aria-hidden="true" /> Tout réinitialiser
          </button>
        </div>
        <p className="text-xs text-slate-500">
          « Revenir en mode hors-ligne » restaure une source de données locale qui fonctionne sans
          serveur. « Tout réinitialiser » repart d'une installation vierge.
        </p>
      </section>
    </div>
  );
}
