import { ActionUILocations } from "@/business/action_registry";
import { Tag } from "@/business/tag";
import type {ActionPerformerRequestParams} from "@/components/business/actions/ActionPerformer.tsx";
import {refid} from "@/business/action_runner";

/**
 * Given a tag, gives the filter name for actions so we can display only actions
 * possible on this type of tag
 * @param tag
 */
export function detailActionLocation(tag:Tag) {
  return tag.isGlobal ? ActionUILocations.tag_managed_detail : ActionUILocations.tag_free_detail
}


export const createActionTemplateTagGroup = (tagGroupId: string): ActionPerformerRequestParams => {
  return {
    tagGroupRef: refid(tagGroupId),
  }
}

export const createActionTemplateTagGroupList = (): ActionPerformerRequestParams => {
  return {}
}

export const createActionTemplateTagManagedList = (tagGroupId: string): ActionPerformerRequestParams => {
  return {
    groupRef: refid(tagGroupId),
  }
}

export const createActionTemplateTag = (tagId: string): ActionPerformerRequestParams => {
  return {
    tagRef: refid(tagId),
  }
}

export const createActionTemplateTagFreeList = (scope: {type: string, id: string | null}): ActionPerformerRequestParams => {
  return {
    scopeRef: {value: scope, readonly: true},
  }
}
