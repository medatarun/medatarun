import type {TagDto, TagGroupDto, TagScopeRef} from "./tag.dto.ts";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {type ActionPayload, executeAction} from "../action_perform.api.ts";




export type TagSearchReq = {
  filters?: TagSearchFilters | null
}

export type TagSearchFilters = {
  operator: "and" | "or"
  items: TagSearchFilter[]
}

export type TagSearchFilter = {
  type: "scopeRef"
  condition: "is"
  value: TagScopeRef
}


export type TagSearchResp = {
  items: TagDto[]
}


export type TagGroupListResp = {
  items: TagGroupDto[]
}
export function tagSearchByScope(scope: TagScopeRef) {
  return {
    filters: {
      operator: "and" as const,
      items: [
        {
          type: "scopeRef" as const,
          condition: "is" as const,
          value: scope
        }
      ]
    }
  }
}

export const useTagSearch = (req: TagSearchReq, enabled: boolean = true) => {
  return useQuery({
    queryKey: ["action", "tag", "tag_search", req.filters ?? null],
    enabled: enabled,
    queryFn: async () => {
      const payload: ActionPayload = {}
      if (req.filters !== undefined) {
        payload.filters = req.filters
      }
      const response = await executeAction<TagSearchResp>("tag", "tag_search", payload)
      if (response.contentType !== "json") {
        throw Error("Expected JSON response for tag/tag_search")
      }
      return response.json
    }
  })
}

export const useTagGroupList = () => {
  return useQuery({
    queryKey: ["action", "tag", "tag_group_list"],
    queryFn: async () => {
      const response = await executeAction<TagGroupListResp>("tag", "tag_group_list", {})
      if (response.contentType !== "json") {
        throw Error("Expected JSON response for tag/tag_group_list")
      }
      return response.json
    }
  })
}

function useTagGroupMutation(actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { tagGroupId: string, value: string }) =>
      executeAction("tag", actionKey, {
        tagGroupRef: "id:" + props.tagGroupId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}

function useManagedTagMutation(actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { tagId: string, value: string }) =>
      executeAction("tag", actionKey, {
        tagRef: "id:" + props.tagId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}

function useFreeTagMutation(actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { tagId: string, value: string }) =>
      executeAction("tag", actionKey, {
        tagRef: "id:" + props.tagId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}

export const useTagGroupUpdateName = () => {
  return useTagGroupMutation("tag_group_update_name")
}

export const useTagGroupUpdateDescription = () => {
  return useTagGroupMutation("tag_group_update_description")
}

export const useTagGroupUpdateKey = () => {
  return useTagGroupMutation("tag_group_update_key")
}

export const useTagManagedUpdateName = () => {
  return useManagedTagMutation("tag_managed_update_name")
}

export const useTagManagedUpdateDescription = () => {
  return useManagedTagMutation("tag_managed_update_description")
}

export const useTagFreeUpdateName = () => {
  return useFreeTagMutation("tag_free_update_name")
}

export const useTagFreeUpdateDescription = () => {
  return useFreeTagMutation("tag_free_update_description")
}

