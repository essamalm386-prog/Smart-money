import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Discover from './pages/Discover';
import Companies, { CompanyDetail } from './pages/Companies';
import AssetDetail from './pages/AssetDetail';
import Scans from './pages/Scans';
import Watchlist from './pages/Watchlist';
import Reports from './pages/Reports';
import Config from './pages/Config';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="decouvrir" element={<Discover />} />
        <Route path="entreprises" element={<Companies />} />
        <Route path="entreprises/:siren" element={<CompanyDetail />} />
        <Route path="assets/:ticker" element={<AssetDetail />} />
        <Route path="scans" element={<Scans />} />
        <Route path="watchlist" element={<Watchlist />} />
        <Route path="reports" element={<Reports />} />
        <Route path="config" element={<Config />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}
