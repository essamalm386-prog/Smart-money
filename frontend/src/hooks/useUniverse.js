import { useQuery } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /universe -> { sectors: [{key,label,count,tickers}], domains: [...] }
 * Secteurs d'activité et thématiques d'investissement (libellés français).
 */
export function useUniverse() {
  return useQuery({
    queryKey: qk.universe,
    queryFn: async () => {
      const data = await dataProvider.getUniverse();
      return {
        sectors: Array.isArray(data?.sectors) ? data.sectors : [],
        domains: Array.isArray(data?.domains) ? data.domains : [],
      };
    },
    staleTime: 5 * 60_000,
  });
}
