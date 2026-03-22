import { ActionUILocations } from "@/business/action_registry";
import { Tag, type TagScopeRef } from "@/business/tag";
import type {
  ActionDisplayedSubject,
  ActionPerformerRequestParams,
} from "@/components/business/actions/ActionPerformer.tsx";
import { refid } from "@/business/action_runner";

/**
 * Given a tag, gives the filter name for actions so we can display only actions
 * possible on this type of tag
 * @param tag
 */
export function detailActionLocation(tag: Tag) {
  return tag.isGlobal
    ? ActionUILocations.tag_global_detail
    : ActionUILocations.tag_local_detail;
}

export const createActionTemplateTagGroup = (
  tagGroupId: string,
): ActionPerformerRequestParams => {
  return {
    tagGroupRef: refid(tagGroupId),
  };
};

export const createActionTemplateTagGroupList =
  (): ActionPerformerRequestParams => {
    return {};
  };

export const createActionTemplateTagManagedList = (
  tagGroupId: string,
): ActionPerformerRequestParams => {
  return {
    groupRef: refid(tagGroupId),
  };
};

export const createActionTemplateTag = (
  tagId: string,
): ActionPerformerRequestParams => {
  return {
    tagRef: refid(tagId),
  };
};

export const createActionTemplateTagFreeList = (scope: {
  type: string;
  id: string | null;
}): ActionPerformerRequestParams => {
  return {
    scopeRef: { value: scope, readonly: true },
  };
};

export const createDisplayedSubjectTagGroup = (
  tagGroupId: string,
): ActionDisplayedSubject => {
  return {
    kind: "resource",
    type: "tag_group",
    refs: { tagGroupId: tagGroupId },
  };
};

/**
 * Represents a tag page subject with optional parent references.
 * Parent refs are used by post hooks to select a business fallback route
 * after destructive actions.
 */
export const createDisplayedSubjectTag = (params: {
  tagId: string;
  tagGroupId: string | null;
  tagScopeRef: TagScopeRef;
}): ActionDisplayedSubject => {
  const refs: Record<string, string> = {
    tagId: params.tagId,
    tagScopeRefType: params.tagScopeRef.type,
  };
  if (params.tagScopeRef.type == "global" && params.tagGroupId !== null) {
    refs.tagGroupId = params.tagGroupId;
  }
  if (params.tagScopeRef.id) {
    refs.tagScopeId = params.tagScopeRef.id;
    if (params.tagScopeRef.type === "model") {
      refs.modelId = params.tagScopeRef.id;
    }
  }

  return {
    kind: "resource",
    type: "tag",
    refs: refs,
  };
};
