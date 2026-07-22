import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /scans -> Scan[]
 */
export function useScans() {
  return useQuery({
    queryKey: qk.scans,
    queryFn: async () => {
      const data = await dataProvider.getScans();
      return Array.isArray(data) ? data : [];
    },
    // Poll while any scan may still be running (remote mode).
    refetchInterval: (query) => {
      const items = query.state.data;
      const running = Array.isArray(items) && items.some((s) => ['pending', 'running', 'queued'].includes(s.status));
      return running ? 5_000 : false;
    },
  });
}

/**
 * GET /scans/{id} -> { scan, theses }
 * @param {string|number} id
 */
export function useScan(id) {
  return useQuery({
    queryKey: qk.scan(id),
    enabled: id !== undefined && id !== null,
    queryFn: async () => dataProvider.getScan(id),
  });
}

/**
 * POST /scans body { tickers?, label?, sector?, domain? } -> Scan
 * Accepte un scan par symboles, par secteur ou par thématique.
 * Invalidates scans, theses and assets so new results surface.
 */
export function useCreateScan() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ tickers, label, sector, domain } = {}) =>
      dataProvider.createScan({ tickers, label, sector, domain }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: qk.scans });
      qc.invalidateQueries({ queryKey: ['theses'] });
      qc.invalidateQueries({ queryKey: qk.assets });
    },
  });
}
