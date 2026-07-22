import { useQuery } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /health -> { status, time, version }
 */
export function useHealth() {
  return useQuery({
    queryKey: qk.health,
    queryFn: async () => dataProvider.getHealth(),
    refetchInterval: 60_000,
    staleTime: 30_000,
  });
}
