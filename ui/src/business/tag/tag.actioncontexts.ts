import type { TagScopeRef } from "@/business/tag/tag.dto.ts";
import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/business/action-performer";
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
  const tag_local_or_global: string =
    params.tagScopeRef.type == "global" ? "tag_global" : "tag_local";
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
    type: tag_local_or_global,
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
  const tagId = options?.tag?.id ?? undefined;
  const tagName = options?.tag?.name;
  const tagDescription = options?.tag?.description;
  const tagKey = options?.tag?.key;
  const tagCreateKey = options?.tagCreateKey;
  return new ActionCtxMapping(
    [
      {
        actionGroupKey: "tags",
        actionParamKey: "tagRef",
        defaultValue: () => (tagId ? "id:" + tagId : undefined),
        readonly: !isNil(tagId),
        visible: isNil(tagId),
      },
      {
        actionGroupKey: "tags",
        actionParamKey: "scopeRef",
        defaultValue: () => scope,
        readonly: true,
        visible: false,
      },
      {
        actionGroupKey: "tags",
        actionKey: "tags/tag_local_create",
        actionParamKey: "key",
        defaultValue: () => tagCreateKey,
      },
      {
        actionGroupKey: "tags",
        actionKey: "tags/tag_global_create",
        actionParamKey: "key",
        defaultValue: () => tagCreateKey,
      },
      {
        actionGroupKey: "tags",
        actionParamKey: "key",
        defaultValue: () => tagKey,
      },
      {
        actionGroupKey: "tags",
        actionParamKey: "name",
        defaultValue: () => tagName,
      },
      {
        actionGroupKey: "tags",
        actionParamKey: "description",
        defaultValue: () => tagDescription,
      },
      {
        actionKey: "tags/tag_global_update_key",
        actionParamKey: "value",
        defaultValue: () => options?.tag?.key,
      },
      {
        actionKey: "tags/tag_local_update_key",
        actionParamKey: "value",
        defaultValue: () => options?.tag?.key,
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
        actionGroupKey: "tags",
        actionParamKey: "tagGroupRef",
        defaultValue: () => "id:" + tagGroup.id,
        readonly: true,
        visible: false,
      },
      {
        actionGroupKey: "tags",
        actionParamKey: "groupRef",
        defaultValue: () => "id:" + tagGroup.id,
        readonly: true,
        visible: false,
      },
      {
        actionKey: "tags/tag_group_update_key",
        actionParamKey: "value",
        defaultValue: () => tagGroup.key,
      },
    ],
    displayedSubject,
  );
}
