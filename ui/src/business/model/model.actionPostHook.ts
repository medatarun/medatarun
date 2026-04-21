import type { NavigateFn } from "@tanstack/react-router";
import type { ActionPostHook } from "@/components/business/actions/ActionPostHook.ts";
import type { ActionDisplayedSubjectResource } from "@/components/business/actions";
import { actionTargetsDisplayedSubject } from "@/components/business/actions/ActionPostHook.matching.ts";

export const modelActionPostHook: ActionPostHook = {
  onActionSuccess: async (context, queryClient) => {
    // An action succeeded.
    // Depending on the action, we need to invalidate our caches.

    // Read actions do not impact caches
    if (context.action.semantics.intent === "read") return true;

    // Try to know if something in our caches may need invalidation
    const concerned = context.action.semantics.subjects.some((subject) =>
      MODEL_CACHED_SUBJECT_TYPES.has(subject.type),
    );
    if (!concerned) return true;

    await queryClient.invalidateQueries({ queryKey: ["model"] });
    await queryClient.invalidateQueries({ queryKey: ["model_summaries"] });
    await queryClient.invalidateQueries({ queryKey: ["search"] });

    return true;
  },

  match: (subject) => {
    return MODEL_DISPLAYED_SUBJECT_TYPES.has(subject.type);
  },

  resolveNavigationAfterSuccess: (context) => {
    // Determine what subject is displayed on screen
    const displayedSubject = context.displayedSubject;
    // Nothing particular, then exit
    if (displayedSubject.kind == "none") return;
    // Nothing we know about then exit
    if (!MODEL_DISPLAYED_SUBJECT_TYPES.has(displayedSubject.type)) return;
    // Try to get from action semantics what the action does
    const intent = context.action.semantics.intent;
    if (intent === "delete") {
      const sameSubject = actionTargetsDisplayedSubject(context);
      if (sameSubject) {
        navigateAfterDelete(context.navigate, displayedSubject);
      }
    } else if (intent === "create") {
      // Intentionally no-op for now. Create navigation is defined per use case.
    } else if (intent === "update" || intent === "read" || intent === "other") {
      // Intentionally no-op.
    } else {
      // Unknown intent: no-op.
    }
  },
};

const MODEL_CACHED_SUBJECT_TYPES = new Set([
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
]);

const MODEL_DISPLAYED_SUBJECT_TYPES = new Set([
  "model",
  "entity",
  "entity_attribute",
  "relationship",
  "relationship_attribute",
  "type",
  "business_key",
]);

function navigateAfterDelete(
  navigate: NavigateFn,
  displayedSubject: ActionDisplayedSubjectResource,
) {
  const type = displayedSubject.type;
  const refs = displayedSubject.refs;
  if (type === "model") {
    navigate({ to: "/models" });
  } else if (type === "entity" || type === "relationship" || type === "type") {
    navigate({
      to: "/model/$modelId",
      params: { modelId: refs.modelId },
    });
  } else if (type === "entity_attribute") {
    navigate({
      to: "/model/$modelId/entity/$entityId",
      params: { modelId: refs.modelId, entityId: refs.entityId },
    });
  } else if (type === "relationship_attribute") {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: { modelId: refs.modelId, relationshipId: refs.relationshipId },
    });
  }
}
