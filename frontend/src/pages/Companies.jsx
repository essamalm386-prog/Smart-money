import { useState } from 'react';
import PropTypes from 'prop-types';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Building2,
  ChevronRight,
  Globe,
  Info,
  MapPin,
  Search,
  ServerOff,
  TrendingDown,
  TrendingUp,
  Users,
} from 'lucide-react';
import { usePappersCompany, usePappersSearch, usePappersStatus } from '../hooks/usePappers';
import * as dataProvider from '../services/dataProvider';
import { Empty, ErrorState, Loading, SkeletonList } from '../components/States';
import { formatEuro, formatEuroCompact } from '../lib/format';

/**
 * Traduit une réponse Pappers { available:false, reason } en message honnête
 * pour l'utilisateur, selon la cause probable.
 * @param {string} [reason]
 * @returns {{ kind:'offline'|'key'|'other', message:string }}
 */
function interpretUnavailable(reason) {
  const isLocal = dataProvider.getMode() === 'local';
  const text = String(reason || '');
  if (isLocal || /mode serveur|hors.?ligne|connectez l'app/i.test(text)) {
    return {
      kind: 'offline',
      message:
        'Cette fonction nécessite le mode serveur. Allez dans Configuration → Se connecter au serveur.',
    };
  }
  if (/cl[eé]|api.?key|configur|pappers_api_key/i.test(text)) {
    return {
      kind: 'key',
      message:
        "La source Pappers n'est pas configurée sur le serveur. Ajoutez une clé PAPPERS_API_KEY (gratuite sur pappers.fr/api) dans les variables du serveur.",
    };
  }
  return {
    kind: 'other',
    message: text || 'La source Pappers est momentanément indisponible.',
  };
}

/** Encart clair et honnête pour les cas où Pappers n'est pas disponible. */
function UnavailableNotice({ reason }) {
  const { kind, message } = interpretUnavailable(reason);
  const Icon = kind === 'offline' ? ServerOff : Info;
  return (
    <div
      role="status"
      className="card flex flex-col items-center gap-3 border-amber-500/25 bg-amber-500/[0.06] px-6 py-10 text-center"
    >
      <Icon className="h-8 w-8 text-amber-400" aria-hidden="true" />
      <p className="max-w-md text-sm text-amber-100">{message}</p>
      {kind === 'offline' ? (
        <Link to="/config" className="btn-ghost">
          Aller à la configuration
        </Link>
      ) : null}
    </div>
  );
}
UnavailableNotice.propTypes = { reason: PropTypes.string };

/** Une ligne / carte de résultat de recherche cliquable. */
function ResultCard({ item, onOpen }) {
  return (
    <button
      type="button"
      onClick={() => onOpen(item.siren)}
      className="card card-hover flex w-full items-center gap-3 p-4 text-left"
    >
      <span
        className="grid h-10 w-10 shrink-0 place-items-center rounded-lg bg-emerald-500/10 text-emerald-400"
        aria-hidden="true"
      >
        <Building2 className="h-5 w-5" />
      </span>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-semibold text-slate-100">{item.nom || 'Entreprise'}</p>
        <p className="mt-0.5 flex flex-wrap items-center gap-x-2 gap-y-0.5 text-xs text-slate-400">
          {item.forme_juridique ? <span>{item.forme_juridique}</span> : null}
          {item.ville ? (
            <span className="text-slate-500">
              · {item.ville}
              {item.code_postal ? ` (${item.code_postal})` : ''}
            </span>
          ) : null}
          {item.libelle_naf ? <span className="text-slate-500">· {item.libelle_naf}</span> : null}
        </p>
        <p className="mt-0.5 font-mono text-[11px] text-slate-500">SIREN {item.siren}</p>
      </div>
      <ChevronRight className="h-4 w-4 shrink-0 text-slate-500" aria-hidden="true" />
    </button>
  );
}
ResultCard.propTypes = {
  item: PropTypes.object.isRequired,
  onOpen: PropTypes.func.isRequired,
};

/**
 * Page « Entreprises françaises » — recherche via l'API Pappers.
 * Route : /entreprises
 */
export default function Companies() {
  const navigate = useNavigate();
  const [term, setTerm] = useState('');
  const [query, setQuery] = useState('');
  const status = usePappersStatus();
  const { data, isLoading, isFetching, isError, error, refetch } = usePappersSearch(query);

  const submit = (e) => {
    e.preventDefault();
    setQuery(term.trim());
  };

  const openCompany = (siren) => navigate(`/entreprises/${encodeURIComponent(siren)}`);

  const results = data?.available ? data.results || [] : [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="flex items-center gap-2 text-xl font-bold text-slate-100 sm:text-2xl">
          <Building2 className="h-5 w-5 text-emerald-400" aria-hidden="true" />
          Entreprises françaises
        </h1>
        <p className="text-sm text-slate-400">
          Consultez le registre légal et les comptes annuels des sociétés françaises. Source :{' '}
          <span className="text-slate-300">Pappers</span> (registre légal + comptes annuels).
        </p>
      </div>

      {/* Rappel discret sur l'état de la source */}
      {status.data && status.data.configured === false ? (
        <p className="flex items-start gap-2 rounded-lg border border-slate-700/60 bg-slate-800/30 px-3 py-2 text-xs text-slate-400">
          <Info className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
          <span>
            La source Pappers n'est pas encore active. Elle nécessite le mode serveur et une clé
            PAPPERS_API_KEY (gratuite sur pappers.fr/api).
          </span>
        </p>
      ) : null}

      <form onSubmit={submit} className="flex flex-col gap-2 sm:flex-row">
        <div className="relative flex-1">
          <Search
            className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500"
            aria-hidden="true"
          />
          <label htmlFor="company-search" className="sr-only">
            Rechercher une entreprise française (nom ou SIREN)
          </label>
          <input
            id="company-search"
            type="search"
            className="input pl-9"
            placeholder="Nom d'entreprise ou SIREN (ex. « Michelin » ou « 855200507 »)"
            value={term}
            onChange={(e) => setTerm(e.target.value)}
            autoComplete="off"
          />
        </div>
        <button type="submit" className="btn-primary shrink-0 justify-center">
          <Search className="h-4 w-4" aria-hidden="true" />
          Rechercher
        </button>
      </form>

      {/* Résultats */}
      {!query ? (
        <Empty
          title="Recherchez une entreprise"
          message="Saisissez un nom de société ou un numéro SIREN pour afficher ses informations légales et financières."
          icon={Building2}
        />
      ) : isLoading || (isFetching && results.length === 0 && !data) ? (
        <SkeletonList rows={5} />
      ) : isError ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : data && data.available === false ? (
        <UnavailableNotice reason={data.reason} />
      ) : results.length === 0 ? (
        <Empty
          title="Aucun résultat"
          message={`Aucune entreprise trouvée pour « ${query} ».`}
          icon={Search}
        />
      ) : (
        <div className="space-y-3">
          <p className="text-xs text-slate-500">
            {data.total ?? results.length} résultat{(data.total ?? results.length) > 1 ? 's' : ''}
            {isFetching ? ' · actualisation…' : ''}
          </p>
          {results.map((item) => (
            <ResultCard key={item.siren} item={item} onOpen={openCompany} />
          ))}
        </div>
      )}
    </div>
  );
}

// --- Vue détail ------------------------------------------------------------

/** Petite ligne identité (libellé + valeur). */
function IdentityRow({ label, value }) {
  if (value === null || value === undefined || value === '') return null;
  return (
    <div>
      <p className="text-[11px] uppercase tracking-wide text-slate-500">{label}</p>
      <p className="text-sm font-medium text-slate-100">{value}</p>
    </div>
  );
}
IdentityRow.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
};

/** Indicateur de tendance du chiffre d'affaires (dernière année vs précédente). */
function RevenueTrend({ finances }) {
  const rows = (finances || [])
    .filter((f) => f && f.chiffre_affaires !== null && f.chiffre_affaires !== undefined)
    .sort((a, b) => Number(a.annee) - Number(b.annee));
  if (rows.length < 2) return null;
  const prev = Number(rows[rows.length - 2].chiffre_affaires);
  const last = Number(rows[rows.length - 1].chiffre_affaires);
  if (!prev) return null;
  const pct = ((last - prev) / Math.abs(prev)) * 100;
  const up = pct >= 0;
  const Icon = up ? TrendingUp : TrendingDown;
  const tone = up ? 'text-emerald-400' : 'text-rose-400';
  return (
    <span className={`inline-flex items-center gap-1 text-sm font-semibold ${tone}`}>
      <Icon className="h-4 w-4" aria-hidden="true" />
      {up ? '+' : ''}
      {pct.toLocaleString('fr-FR', { maximumFractionDigits: 1 })} % sur un an
    </span>
  );
}
RevenueTrend.propTypes = { finances: PropTypes.array };

/**
 * Page détail d'une entreprise française.
 * Route : /entreprises/:siren
 */
export function CompanyDetail() {
  const { siren } = useParams();
  const { data, isLoading, isError, error, refetch } = usePappersCompany(siren);

  const back = (
    <Link
      to="/entreprises"
      className="inline-flex items-center gap-1 text-sm text-slate-400 hover:text-slate-200"
    >
      <ArrowLeft className="h-4 w-4" aria-hidden="true" /> Retour aux entreprises
    </Link>
  );

  if (isLoading) {
    return (
      <div className="space-y-6">
        {back}
        <Loading label={`Chargement de l'entreprise ${siren}…`} />
      </div>
    );
  }
  if (isError) {
    return (
      <div className="space-y-6">
        {back}
        <ErrorState error={error} onRetry={refetch} />
      </div>
    );
  }
  if (data && data.available === false) {
    return (
      <div className="space-y-6">
        {back}
        <UnavailableNotice reason={data.reason} />
      </div>
    );
  }

  const company = data?.company;
  if (!company) {
    return (
      <div className="space-y-6">
        {back}
        <Empty title="Entreprise introuvable" message={`Aucune donnée pour le SIREN ${siren}.`} />
      </div>
    );
  }

  const dirigeants = company.dirigeants || [];
  const finances = (company.finances || [])
    .slice()
    .sort((a, b) => Number(b.annee) - Number(a.annee));

  return (
    <div className="space-y-6">
      {back}

      {/* Identité */}
      <section className="card space-y-4 p-5">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="flex items-start gap-3">
            <span
              className="grid h-11 w-11 shrink-0 place-items-center rounded-lg bg-emerald-500/10 text-emerald-400"
              aria-hidden="true"
            >
              <Building2 className="h-6 w-6" />
            </span>
            <div className="min-w-0">
              <h1 className="text-xl font-bold text-slate-100 sm:text-2xl">{company.nom}</h1>
              <p className="mt-0.5 flex flex-wrap items-center gap-x-2 text-sm text-slate-400">
                {company.forme_juridique ? <span>{company.forme_juridique}</span> : null}
                <span className="font-mono text-xs text-slate-500">· SIREN {company.siren}</span>
              </p>
            </div>
          </div>
          <RevenueTrend finances={company.finances} />
        </div>

        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          <IdentityRow label="Capital social" value={formatEuro(company.capital)} />
          <IdentityRow
            label="Date de création"
            value={
              company.date_creation
                ? new Date(company.date_creation).toLocaleDateString('fr-FR')
                : null
            }
          />
          <IdentityRow label="Effectif" value={company.effectif} />
          <IdentityRow
            label="Code NAF"
            value={
              company.code_naf
                ? `${company.code_naf}${company.libelle_naf ? ` — ${company.libelle_naf}` : ''}`
                : company.libelle_naf || null
            }
          />
        </div>

        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          {company.adresse ? (
            <p className="flex items-start gap-2 text-sm text-slate-300">
              <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-slate-500" aria-hidden="true" />
              {company.adresse}
            </p>
          ) : null}
          {company.site_web ? (
            <p className="flex items-center gap-2 text-sm">
              <Globe className="h-4 w-4 shrink-0 text-slate-500" aria-hidden="true" />
              <a
                href={/^https?:\/\//i.test(company.site_web) ? company.site_web : `https://${company.site_web}`}
                target="_blank"
                rel="noreferrer noopener"
                className="truncate text-emerald-400 hover:underline"
              >
                {company.site_web}
              </a>
            </p>
          ) : null}
        </div>
      </section>

      {/* Dirigeants */}
      <section className="card p-5">
        <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-slate-300">
          <Users className="h-4 w-4" aria-hidden="true" /> Dirigeants
        </h2>
        {dirigeants.length === 0 ? (
          <p className="text-sm text-slate-500">Aucun dirigeant renseigné.</p>
        ) : (
          <ul className="divide-y divide-terminal-border">
            {dirigeants.map((d, i) => (
              <li key={`${d.nom}-${i}`} className="flex items-center justify-between gap-3 py-2.5">
                <span className="text-sm font-medium text-slate-100">{d.nom}</span>
                {d.qualite ? (
                  <span className="text-xs text-slate-400">{d.qualite}</span>
                ) : null}
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* Finances */}
      <section className="card p-5">
        <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-slate-300">
          <TrendingUp className="h-4 w-4" aria-hidden="true" /> Comptes annuels
        </h2>
        {finances.length === 0 ? (
          <p className="text-sm text-slate-500">Aucun compte annuel disponible.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[520px] text-sm">
              <caption className="sr-only">Finances de l'entreprise par année</caption>
              <thead>
                <tr className="border-b border-terminal-border text-left text-[11px] uppercase tracking-wide text-slate-500">
                  <th scope="col" className="py-2 pr-3 font-medium">Année</th>
                  <th scope="col" className="py-2 pr-3 text-right font-medium">Chiffre d'affaires</th>
                  <th scope="col" className="py-2 pr-3 text-right font-medium">Résultat net</th>
                  <th scope="col" className="py-2 pr-3 text-right font-medium">Marge brute</th>
                  <th scope="col" className="py-2 text-right font-medium">Effectif</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-terminal-border/60">
                {finances.map((f) => (
                  <tr key={f.annee}>
                    <th scope="row" className="py-2 pr-3 text-left font-mono font-medium text-slate-200">
                      {f.annee}
                    </th>
                    <td className="py-2 pr-3 text-right font-mono text-slate-100">
                      {formatEuroCompact(f.chiffre_affaires)}
                    </td>
                    <td
                      className={`py-2 pr-3 text-right font-mono ${
                        Number(f.resultat) < 0 ? 'text-rose-400' : 'text-slate-100'
                      }`}
                    >
                      {formatEuroCompact(f.resultat)}
                    </td>
                    <td className="py-2 pr-3 text-right font-mono text-slate-300">
                      {formatEuroCompact(f.marge_brute)}
                    </td>
                    <td className="py-2 text-right font-mono text-slate-300">
                      {f.effectif ?? '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
