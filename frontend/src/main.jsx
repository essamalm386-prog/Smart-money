import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import ErrorBoundary from './components/ErrorBoundary';
import { registerServiceWorker } from './registerSW';
import { initDataProvider } from './services/dataProvider';
import { clearAll } from './services/recovery';
import './index.css';

// Escape hatch: opening the app with ?reset=1 (or #reset) wipes local settings
// and data, so a bad configuration can always be undone from the URL.
try {
  const params = new URLSearchParams(window.location.search);
  if (params.has('reset') || window.location.hash.replace('#', '') === 'reset') {
    clearAll();
    window.history.replaceState({}, '', window.location.pathname);
  }
} catch {
  /* ignore */
}

// Configure the data source (offline-first) and seed the on-device store
// before the first render so the app works with no backend.
initDataProvider();

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 15_000,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    </ErrorBoundary>
  </React.StrictMode>
);

registerServiceWorker();
