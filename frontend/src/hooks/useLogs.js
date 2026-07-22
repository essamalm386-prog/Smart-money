import { useQuery } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /logs?limit -> LogEntry[]
 * @param {number} [limit=100]
 */
export function useLogs(limit = 100) {
  return useQuery({
    queryKey: qk.logs(limit),
    queryFn: async () => {
      const data = await dataProvider.getLogs(limit);
      return Array.isArray(data) ? data : [];
    },
    refetchInterval: 30_000,
  });
}
