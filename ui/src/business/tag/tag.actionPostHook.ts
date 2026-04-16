import type { NavigateFn } from "@tanstack/react-router";
import type { ActionPostHook } from "@/components/business/actions/ActionPostHook.ts";
import type { ActionDisplayedSubjectResource } from "@/components/business/actions";
import { actionTargetsDisplayedSubject } from "@/components/business/actions/ActionPostHook.matching.ts";

export const tagActionPostHook: ActionPostHook = {
  match: (subject) => {
    return TAG_SUBJECT_TYPES.has(subject.type);
  },

  onActionSuccess: async (context, queryClient) => {
    await queryClient.invalidateQueries({ queryKey: ["action", "tag"] });
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

const TAG_SUBJECT_TYPES = new Set([
  "tag",
  "tag_local",
  "tag_global",
  "tag_group",
]);

function navigateAfterDelete(
  navigate: NavigateFn,
  displayedSubject: ActionDisplayedSubjectResource,
) {
  const type = displayedSubject.type;
  const refs = displayedSubject.refs;
  if (type === "tag_group") {
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
