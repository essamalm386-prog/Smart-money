import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * GET /config -> Config
 */
export function useConfig() {
  return useQuery({
    queryKey: qk.config,
    queryFn: async () => dataProvider.getConfig(),
  });
}

/**
 * PUT /config body partial -> updated Config
 */
export function useUpdateConfig() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (partial) => dataProvider.updateConfig(partial),
    onSuccess: (data) => {
      qc.setQueryData(qk.config, data);
      qc.invalidateQueries({ queryKey: qk.config });
    },
  });
}
