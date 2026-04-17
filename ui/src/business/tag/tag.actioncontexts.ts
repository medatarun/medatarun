import type { TagScopeRef } from "@/business/tag/tag.dto.ts";
import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/components/business/actions";
import { type Tag, TagGroup } from "@/business/tag/tag.domain.ts";
import { isNil } from "lodash-es";

/**
 * Tag group page, the tag group is the subject
 */
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
 * The tag page with tag as a subject with optional parent references.
 * Parent refs are used by post-actions hooks to select a business fallback route
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

/**
 * Matches actions of the tag action group
 * @param scope
 * @param displayedSubject
 * @param options
 */
export const createActionCtxTag = (
  scope: TagScopeRef,
  displayedSubject: ActionDisplayedSubject,
  options?: {
    tagCreateKey?: string;
    tag?: Tag;
  },
) => {
  const tagId = options?.tag ?? undefined;
  const tagName = options?.tag?.name;
  const tagDescription = options?.tag?.description;
  const tagKey = options?.tag?.key;
  const tagCreateKey = options?.tagCreateKey;
  return new ActionCtxMapping(
    [
      {
        actionGroupKey: "tag",
        actionParamKey: "tagRef",
        defaultValue: () => (tagId ? "id:" + tagId : undefined),
        readonly: !isNil(tagId),
        visible: isNil(tagId),
      },
      {
        actionGroupKey: "tag",
        actionParamKey: "scopeRef",
        defaultValue: () => scope,
        readonly: true,
        visible: false,
      },
      {
        actionGroupKey: "tag",
        actionKey: "tag_local_create",
        actionParamKey: "key",
        defaultValue: () => tagCreateKey,
      },
      {
        actionGroupKey: "tag",
        actionKey: "tag_global_create",
        actionParamKey: "key",
        defaultValue: () => tagCreateKey,
      },
      {
        actionGroupKey: "tag",
        actionParamKey: "key",
        defaultValue: () => tagKey,
      },
      {
        actionGroupKey: "tag",
        actionParamKey: "name",
        defaultValue: () => tagName,
      },
      {
        actionGroupKey: "tag",
        actionParamKey: "description",
        defaultValue: () => tagDescription,
      },
    ],
    displayedSubject,
  );
};

export function createActionCtxTagGroup(
  tagGroup: TagGroup,
  displayedSubject: ActionDisplayedSubject,
) {
  return new ActionCtxMapping(
    [
      {
        actionGroupKey: "tag",
        actionParamKey: "tagGroupRef",
        defaultValue: () => "id:" + tagGroup.id,
        readonly: true,
        visible: false,
      },
      {
        actionGroupKey: "tag",
        actionParamKey: "groupRef",
        defaultValue: () => "id:" + tagGroup.id,
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
}
