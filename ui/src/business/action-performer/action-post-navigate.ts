import type {
  ActionDisplayedSubject,
  ActionDisplayedSubjectResource,
  ActionPerformerState,
  ActionRequest,
} from "@/business/action-performer/index.ts";
import type { NavigateFn } from "@tanstack/react-router";
import type { ActionDescriptor } from "@/business/action_registry";

/**
 * Called after an action was successfully completed and the action state is done.
 * This acts as a navigation side effect handler.
 */
export const actionPostNavigate = (context: {
  action: ActionDescriptor;
  request: ActionRequest;
  displayedSubject: ActionDisplayedSubject;
  navigate: NavigateFn;
}) => {
  // Determine what subject is displayed on screen
  const displayedSubject = context.displayedSubject;

  // Nothing particular, then exit
  if (displayedSubject.kind == "none") return;

  // Try to get from action semantics what the action does
  const intent = context.action.semantics.intent;
  if (intent === "delete") {
    const sameSubject = actionTargetsDisplayedSubject({
      action: context.action,
      request: context.request,
      displayedSubject,
    });
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

/**
 * Decodes an action request parameter value into a plain id when possible.
 * Supported shapes:
 * - "id:<value>" -> "<value>"
 * - "<value>" -> "<value>"
 */
function decodeActionParamId(value: unknown): string | null {
  if (typeof value !== "string") return null;
  if (value.startsWith("id:")) return value.substring(3);
  return value;
}

/**
 * Returns true when the action target described by semantics matches
 * the currently displayed subject.
 */
export function actionTargetsDisplayedSubject(context: {
  action: ActionDescriptor;
  request: ActionRequest;
  displayedSubject: ActionDisplayedSubject;
}): boolean {
  const displayedSubject = context.displayedSubject;
  if (displayedSubject.kind == "none") return false;

  const targetSubject = context.action.semantics.subjects[0];
  let matches = true;

  if (!targetSubject) {
    matches = false;
  } else if (targetSubject.type !== displayedSubject.type) {
    matches = false;
  } else {
    for (const refParam of targetSubject.referencingParams) {
      const expectedId = decodeActionParamId(
        context.request.ctx.getDefaultValue(refParam.name, context.request),
      );
      const displayedKey = toDisplayedSubjectIdKey(refParam.name);
      const displayedId = displayedKey
        ? displayedSubject.refs[displayedKey]
        : undefined;
      if (
        !expectedId ||
        !displayedKey ||
        !displayedId ||
        expectedId !== displayedId
      ) {
        matches = false;
        break;
      }
    }
  }

  return matches;
}

/**
 * Maps semantics param names to displayedSubject id keys.
 * Example: modelRef -> modelId, entityRef -> entityId.
 */
function toDisplayedSubjectIdKey(paramName: string): string | null {
  if (paramName.endsWith("Ref")) {
    return paramName.substring(0, paramName.length - 3) + "Id";
  }
  if (paramName.endsWith("Id")) {
    return paramName;
  }
  return null;
}
