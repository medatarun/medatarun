import type { NavigateFn } from "@tanstack/react-router";
import type { ActionPostHook } from "@/components/business/actions/ActionPostHook.ts";
import type {
  ActionDisplayedSubjectResource,
} from "@/components/business/actions/ActionPerformer.tsx";
import {
  actionTargetsDisplayedSubject,
  decodeActionParamId,
} from "@/components/business/actions/ActionPostHook.matching.ts";

export const modelActionPostHook: ActionPostHook = {
  match: (subject) => {
    return MODEL_SUBJECT_TYPES.has(subject.type);
  },

  onActionSuccess: async (context, queryClient) => {
    const modelId = decodeActionParamId(context.request.params.modelRef);
    if (modelId) {
      await queryClient.invalidateQueries({ queryKey: ["model", modelId] });
    }

    if (context.action.semantics.intent !== "read") {
      await queryClient.invalidateQueries({ queryKey: ["model_summaries"] });
      await queryClient.invalidateQueries({ queryKey: ["search"] });
    }
    return true;
  },

  resolveNavigationAfterSuccess: (context) => {
    const intent = context.action.semantics.intent;
    if (intent === "delete") {
      const sameSubject = actionTargetsDisplayedSubject(context);
      if (sameSubject) {
        navigateAfterDelete(context.navigate, context.displayedSubject);
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

const MODEL_SUBJECT_TYPES = new Set([
  "model",
  "entity",
  "entity_attribute",
  "relationship",
  "relationship_attribute",
  "type",
]);

function navigateAfterDelete(
  navigate: NavigateFn,
  displayedSubject: ActionDisplayedSubjectResource,
) {
  const type = displayedSubject.type;
  const refs = displayedSubject.refs;
  if (type === "model") {
    navigate({ to: "/models" });
  } else if (
    type === "entity" ||
    type === "relationship" ||
    type === "type"
  ) {
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
