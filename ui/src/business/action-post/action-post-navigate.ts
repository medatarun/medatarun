import type {
  ActionDisplayedSubjectResource,
  ActionPostNavigationContext,
} from "@/business/action-performer";
import { actionTargetsDisplayedSubject } from "@/business/action-performer";
import type { NavigateFn } from "@tanstack/react-router";

/**
 * Called after onActionSuccess(...) has completed and action state is done.
 * This method is an optional navigation side-effect handler.
 *
 * Contract for implementers:
 * - Call navigate(...) only when navigation is explicitly required.
 * - Do nothing to keep the current route.
 * - Do not perform cache work here (handled in onActionSuccess).
 */
export const resolveNavigationAfterSuccess = (
  context: ActionPostNavigationContext,
) => {
  // Determine what subject is displayed on screen
  const displayedSubject = context.displayedSubject;

  // Nothing particular, then exit
  if (displayedSubject.kind == "none") return;

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
};

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
  } else if (type === "role") {
    navigate({ to: "/admin/roles" });
  } else if (type === "tag_group") {
    navigate({ to: "/tag-groups" });
  } else if (type === "tag") {
    const tagGroupId = refs.tagGroupId;
    const modelId = refs.modelId;
    if (tagGroupId) {
      navigate({
        to: "/tag-group/$tagGroupId",
        params: { tagGroupId: tagGroupId },
      });
    } else if (modelId) {
      navigate({
        to: "/model/$modelId",
        params: { modelId: modelId },
      });
    } else {
      navigate({ to: "/tag-groups" });
    }
  }
}
