import type { ActionPostNavigationContext } from "./ActionPostHook.ts";
import type { ActionPerformerRequestParam } from "./ActionPerformer.tsx";

/**
 * Decodes an action request parameter value into a plain id when possible.
 * Supported shapes:
 * - "id:<value>" -> "<value>"
 * - "<value>" -> "<value>"
 */
export function decodeActionParamId(
  param: ActionPerformerRequestParam | undefined,
): string | null {
  if (!param) return null;
  const value = param.value;
  if (typeof value !== "string") return null;
  if (value.startsWith("id:")) return value.substring(3);
  return value;
}

/**
 * Returns true when the action target described by semantics matches
 * the currently displayed subject.
 */
export function actionTargetsDisplayedSubject(
  context: ActionPostNavigationContext,
): boolean {
  const displayedSubject = context.displayedSubject;
  const targetSubject = context.action.semantics.subjects[0];
  let matches = true;

  if (!targetSubject) {
    matches = false;
  } else if (targetSubject.type !== displayedSubject.type) {
    matches = false;
  } else {
    for (const refParam of targetSubject.referencingParams) {
      const expectedId = decodeActionParamId(
        context.request.params[refParam.name],
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
