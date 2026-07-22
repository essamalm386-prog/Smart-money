import { useQuery, keepPreviousData } from '@tanstack/react-query';
import * as dataProvider from '../services/dataProvider';
import { qk } from './queryKeys';

/**
 * Statut de la source Pappers.
 * GET /pappers/status -> { provider:"Pappers", configured: boolean }
 * En mode hors-ligne, renvoie toujours { configured:false }.
 */
export function usePappersStatus() {
  return useQuery({
    queryKey: qk.pappersStatus,
    queryFn: async () => dataProvider.pappersStatus(),
    staleTime: 60_000,
  });
}

/**
 * Recherche d'entreprises françaises (nom ou SIREN).
 * Activée uniquement lorsque la requête n'est pas vide. Conserve les résultats
 * précédents pendant la frappe pour éviter les clignotements.
 * @param {string} q
 */
export function usePappersSearch(q) {
  const query = (q || '').trim();
  return useQuery({
    queryKey: qk.pappersSearch(query),
    enabled: query.length > 0,
    queryFn: async () => dataProvider.pappersSearch(query),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}

/**
 * Fiche détaillée d'une entreprise par SIREN.
 * Activée uniquement lorsque le SIREN n'est pas vide.
 * @param {string} siren
 */
export function usePappersCompany(siren) {
  const value = (siren || '').trim();
  return useQuery({
    queryKey: qk.pappersCompany(value),
    enabled: value.length > 0,
    queryFn: async () => dataProvider.pappersCompany(value),
    staleTime: 60_000,
  });
}
