import { useQuery } from "@tanstack/react-query";
import { ActionRegistry } from "@/business/action_registry/action_registry.biz.tsx";
import { actionRegistryStatic } from "@/business/action_registry/action_registry.static.ts";

export type ActionAccessScope = "public" | "authenticated";

/**
 * Loads the action registry using a cache key tied to the access scope.
 *
 * Why scope is part of the key:
 * - "public" fetch can happen before the OIDC token is available.
 * - once authenticated, we must refetch with user permissions applied.
 * - changing the key forces an explicit fetch for the authenticated view.
 */
export function useActionRegistryQuery(actionAccessScope: ActionAccessScope) {
  return useQuery({
    queryKey: ["action-registry", actionAccessScope],
    queryFn: async () => {
      return new ActionRegistry(actionRegistryStatic);
    },
    // Keep current list visible while authenticated list is being loaded.
    placeholderData: (previousData) => previousData,
  });
}
