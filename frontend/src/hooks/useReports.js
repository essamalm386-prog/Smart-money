import { useQuery } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /reports?limit -> Report[]
 * @param {number} [limit=50]
 */
export function useReports(limit = 50) {
  return useQuery({
    queryKey: qk.reports(limit),
    queryFn: async () => {
      const data = await dataProvider.getReports(limit);
      return Array.isArray(data) ? data : [];
    },
  });
}
