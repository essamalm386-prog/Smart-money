import { useState } from 'react';
import PropTypes from 'prop-types';
import { NavLink, Outlet, Link } from 'react-router-dom';
import {
  Activity,
  Building2,
  ClipboardList,
  Compass,
  Eye,
  LayoutDashboard,
  Menu,
  Radar,
  Settings,
  TrendingUp,
  History,
} from 'lucide-react';
import { useHealth } from '../hooks/useHealth';
import { getMode } from '../services/dataProvider';
import { resetToOffline } from '../services/recovery';
import RunScanModal from './RunScanModal';

/**
 * Shown when the app is in "server" mode but the server can't be reached, so the
 * user is never stuck behind an error: one tap returns to a working offline app.
 */
function RecoveryBanner() {
  const { isError, isLoading } = useHealth();
  const remote = getMode() === 'remote';
  if (!remote || isLoading || !isError) return null;
  return (
    <div className="border-b border-amber-500/30 bg-amber-500/10 px-4 py-2 text-sm text-amber-200">
      <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-2">
        <span>Serveur injoignable. Vérifiez l'URL dans Configuration, ou revenez en mode hors-ligne.</span>
        <button
          type="button"
          className="rounded-lg bg-amber-500/20 px-3 py-1 text-xs font-semibold text-amber-100 hover:bg-amber-500/30"
          onClick={() => resetToOffline()}
        >
          Revenir en mode hors-ligne
        </button>
      </div>
    </div>
  );
}

/** Primary navigation definition. */
const NAV = [
  { to: '/', label: 'Tableau de bord', icon: LayoutDashboard, end: true },
  { to: '/decouvrir', label: 'Découvrir', icon: Compass },
  { to: '/entreprises', label: 'Entreprises', icon: Building2 },
  { to: '/scans', label: 'Scans', icon: History },
  { to: '/watchlist', label: 'Liste de suivi', icon: Eye },
  { to: '/reports', label: 'Rapports', icon: ClipboardList },
  { to: '/config', label: 'Configuration', icon: Settings },
];

function HealthDot() {
  const { data, isError } = useHealth();
  const ok = data?.status === 'ok' && !isError;
  return (
    <span className="inline-flex items-center gap-1.5 text-xs text-slate-400" title={ok ? 'Serveur en ligne' : 'Serveur injoignable'}>
      <span
        className={`h-2 w-2 rounded-full ${ok ? 'bg-emerald-400' : 'bg-rose-500 animate-pulse-soft'}`}
        aria-hidden="true"
      />
      <span className="hidden sm:inline">{ok ? `en ligne · v${data?.version ?? '—'}` : 'hors ligne'}</span>
      <span className="sr-only">{ok ? 'Serveur en ligne' : 'Serveur hors ligne'}</span>
    </span>
  );
}

function SidebarContent({ collapsed, onNavigate }) {
  return (
    <nav className="flex h-full flex-col gap-1" aria-label="Navigation principale">
      {NAV.map(({ to, label, icon: Icon, end }) => (
        <NavLink
          key={to}
          to={to}
          end={end}
          onClick={onNavigate}
          className={({ isActive }) =>
            `group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
              isActive
                ? 'bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/20'
                : 'text-slate-400 hover:bg-slate-800 hover:text-slate-100'
            }`
          }
        >
          <Icon className="h-5 w-5 shrink-0" aria-hidden="true" />
          {!collapsed ? <span className="truncate">{label}</span> : null}
        </NavLink>
      ))}
    </nav>
  );
}
SidebarContent.propTypes = {
  collapsed: PropTypes.bool,
  onNavigate: PropTypes.func,
};

/**
 * App shell: collapsible desktop sidebar + mobile bottom nav + top bar.
 */
export default function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  const [scanOpen, setScanOpen] = useState(false);

  return (
    <div className="min-h-screen bg-terminal-bg text-slate-200">
      {/* Desktop sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 hidden flex-col border-r border-terminal-border bg-terminal-panel/60 p-3 md:flex ${
          collapsed ? 'w-16' : 'w-60'
        } transition-[width] duration-200`}
      >
        <div className="mb-4 flex items-center gap-2 px-1">
          <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-emerald-500/10 text-emerald-400">
            <TrendingUp className="h-5 w-5" aria-hidden="true" />
          </span>
          {!collapsed ? (
            <div className="min-w-0">
              <p className="truncate font-semibold leading-tight text-slate-100">Smart Money</p>
              <p className="truncate text-[11px] text-slate-500">Terminal de signaux</p>
            </div>
          ) : null}
        </div>

        <SidebarContent collapsed={collapsed} />

        <div className="mt-auto space-y-2">
          {!collapsed ? (
            <button type="button" className="btn-primary w-full" onClick={() => setScanOpen(true)}>
              <Radar className="h-4 w-4" aria-hidden="true" />
              Lancer un scan
            </button>
          ) : (
            <button
              type="button"
              className="btn-primary w-full !px-0"
              onClick={() => setScanOpen(true)}
              aria-label="Lancer un scan"
            >
              <Radar className="h-4 w-4" aria-hidden="true" />
            </button>
          )}
          <button
            type="button"
            className="btn-ghost w-full"
            onClick={() => setCollapsed((c) => !c)}
            aria-label={collapsed ? 'Déplier le menu' : 'Replier le menu'}
          >
            <Menu className="h-4 w-4" aria-hidden="true" />
            {!collapsed ? <span>Replier</span> : null}
          </button>
        </div>
      </aside>

      {/* Main column */}
      <div className={`flex min-h-screen flex-col ${collapsed ? 'md:pl-16' : 'md:pl-60'} transition-[padding] duration-200`}>
        {/* Top bar */}
        <header className="sticky top-0 z-20 flex items-center justify-between gap-3 border-b border-terminal-border bg-terminal-bg/85 px-4 py-3 backdrop-blur">
          <Link to="/" className="flex items-center gap-2 md:hidden">
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-emerald-500/10 text-emerald-400">
              <TrendingUp className="h-4 w-4" aria-hidden="true" />
            </span>
            <span className="font-semibold text-slate-100">Smart Money</span>
          </Link>
          <div className="hidden items-center gap-2 text-sm text-slate-400 md:flex">
            <Activity className="h-4 w-4 text-emerald-400" aria-hidden="true" />
            <span>Tableau de bord de découverte d'investissements</span>
          </div>
          <div className="flex items-center gap-3">
            <HealthDot />
            <button
              type="button"
              className="btn-primary !py-1.5 md:hidden"
              onClick={() => setScanOpen(true)}
              aria-label="Lancer un scan"
            >
              <Radar className="h-4 w-4" aria-hidden="true" />
              <span className="sr-only sm:not-sr-only">Scan</span>
            </button>
          </div>
        </header>

        <RecoveryBanner />

        {/* Routed page */}
        <main className="mx-auto w-full max-w-7xl flex-1 px-4 pb-24 pt-5 md:pb-8">
          <Outlet context={{ openScan: () => setScanOpen(true) }} />
        </main>
      </div>

      {/* Mobile bottom nav — défilement horizontal pour rester lisible avec
          plusieurs entrées sur les petits écrans. */}
      <nav
        className="fixed inset-x-0 bottom-0 z-30 flex overflow-x-auto border-t border-terminal-border bg-terminal-panel/95 backdrop-blur md:hidden"
        aria-label="Navigation principale mobile"
      >
        {NAV.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex min-w-[4.5rem] flex-1 shrink-0 flex-col items-center gap-0.5 py-2 text-center text-[10px] font-medium ${
                isActive ? 'text-emerald-300' : 'text-slate-400'
              }`
            }
          >
            <Icon className="h-5 w-5" aria-hidden="true" />
            <span className="leading-tight">{label}</span>
          </NavLink>
        ))}
      </nav>

      <RunScanModal open={scanOpen} onClose={() => setScanOpen(false)} />
    </div>
  );
}
