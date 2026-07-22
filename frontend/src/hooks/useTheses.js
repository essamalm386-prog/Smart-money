import { useQuery } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /theses?limit -> Thesis[] (ranked desc by conviction_score)
 * @param {number} [limit=50]
 */
export function useTheses(limit = 50) {
  return useQuery({
    queryKey: qk.theses(limit),
    queryFn: async () => {
      const data = await dataProvider.getTheses(limit);
      return Array.isArray(data) ? data : [];
    },
    staleTime: 30_000,
  });
}

/**
 * GET /theses/{id} -> full thesis (incl. agent_reports)
 * @param {string|number} id
 */
export function useThesis(id) {
  return useQuery({
    queryKey: qk.thesis(id),
    enabled: id !== undefined && id !== null,
    queryFn: async () => dataProvider.getThesis(id),
  });
}
