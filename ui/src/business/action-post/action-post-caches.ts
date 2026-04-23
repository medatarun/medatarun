import type { QueryClient, QueryKey } from "@tanstack/react-query";
import type { ActionKey } from "@/business/action_registry/actionRegistry.dictionnary.ts";
import { ActionDescriptor, ActionRegistry } from "@/business/action_registry";
import { ACTION_AUTH_QUERY_KEY_USER_LIST } from "@/business/auth_user";

/**
 * Called after backend action execution succeeded, before the generic
 * `queryClient.invalidateQueries()` fallback is decided.
 *
 * Contract for implementers:
 * - Do cache updates/invalidation only for your business scope.
 * - Return true when this hook handled cache refresh responsibility.
 * - Return false when caller should keep the generic fallback invalidation.
 * - Throw on unexpected errors; caller will propagate the failure.
 *
 * Notes:
 * - In a multi-hook setup, several matching hooks can run for the same action.
 * - Returning true does not stop other matching hooks from running.
 */
export async function actionPostSuccess(
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
