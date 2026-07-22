import { useQuery } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /assets -> Asset[]
 */
export function useAssets() {
  return useQuery({
    queryKey: qk.assets,
    queryFn: async () => {
      const data = await dataProvider.getAssets();
      return Array.isArray(data) ? data : [];
    },
    staleTime: 30_000,
  });
}

/**
 * GET /assets/{ticker} -> { asset, latest_thesis, reports, score_history }
 * @param {string} ticker
 */
export function useAsset(ticker) {
  return useQuery({
    queryKey: qk.asset(ticker),
    enabled: Boolean(ticker),
    queryFn: async () => dataProvider.getAsset(ticker),
  });
}
