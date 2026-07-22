import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /watchlist -> WatchlistItem[]
 */
export function useWatchlist() {
  return useQuery({
    queryKey: qk.watchlist,
    queryFn: async () => {
      const data = await dataProvider.getWatchlist();
      return Array.isArray(data) ? data : [];
    },
  });
}

/**
 * POST /watchlist body { ticker, note? } -> WatchlistItem
 */
export function useAddToWatchlist() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ ticker, note }) => dataProvider.addWatchlist({ ticker, note }),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.watchlist }),
  });
}

/**
 * DELETE /watchlist/{ticker} -> { ok: true }
 */
export function useRemoveFromWatchlist() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (ticker) => dataProvider.removeWatchlist(ticker),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.watchlist }),
  });
}
