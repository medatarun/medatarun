import type { QueryClient, QueryKey } from "@tanstack/react-query";
import type { ActionKey } from "@/business/action_registry";
import { ActionDescriptor, ActionRegistry } from "@/business/action_registry";
import { ACTION_AUTH_QUERY_KEY_USER_LIST } from "@/components/business/auth-user";

/**
 * Called after backend action execution succeeded to handle query caches.
 *
 * Look at action semantics and decide which queryClient caches must be
 * invalidated. For example, when a mutation occurs (item deleted, name
 * change, ...) The caches of matching subjects must be discared so the
 * screen can be refreshed.
 */
export async function actionPostCacheManagement(
  actionKey: ActionKey,
  queryClient: QueryClient,
  actionRegistry: ActionRegistry,
) {
  const action = actionRegistry.findActionByActionKey(actionKey);
  if (!action) return;
  if (action.semantics.intent === "read") return;

  const keysToInvalidate: QueryKey[] = [];

  if (
    concerns(action, [
      "model",
      "entity",
      "entity_attribute",
      "relationship",
      "relationship_attribute",
      "type",
      "business_key",
      "tag_local",
      "tag_group",
      "tag_global",
    ])
  ) {
    keysToInvalidate.push(["model"]);
    keysToInvalidate.push(["model_summaries"]);
    keysToInvalidate.push(["search"]);
  }
  if (concerns(action, ["tag", "tag_local", "tag_global", "tag_group"])) {
    keysToInvalidate.push(["action", "tag"]);
  }
  if (concerns(action, ["actor", "user"])) {
    keysToInvalidate.push(ACTION_AUTH_QUERY_KEY_USER_LIST);
    keysToInvalidate.push(["whoami"]);
  }
  if (concerns(action, ["actor", "role"])) {
    keysToInvalidate.push(["action", "auth", "role"]);
    keysToInvalidate.push(["action", "auth", "actor"]);
    keysToInvalidate.push(["whoami"]);
  }

  for (const keyToInvalidate of keysToInvalidate) {
    await queryClient.invalidateQueries({ queryKey: keyToInvalidate });
  }
}

function concerns(action: ActionDescriptor, subjects: string[]): boolean {
  return action.semantics.subjects.some((s) => subjects.includes(s.type));
}
