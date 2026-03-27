import type { TagDto, TagGroupDto, TagScopeRef } from "./tag.dto.ts";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type ActionPayload, executeActionJson } from "../action_runner";

export type TagSearchReq = {
  filters?: TagSearchFilters | null;
};

export type TagSearchFilters = {
  operator: "and" | "or";
  items: TagSearchFilter[];
};

export type TagSearchFilter = {
  type: "scopeRef";
  condition: "is";
  value: TagScopeRef;
};

export type TagSearchResp = {
  items: TagDto[];
};

export type TagGroupListResp = {
  items: TagGroupDto[];
};

export const useTagSearch = (req: TagSearchReq, enabled: boolean = true) => {
  return useQuery({
    queryKey: ["action", "tag", "tag_search", req.filters ?? null],
    enabled: enabled,
    queryFn: async () => {
      const payload: ActionPayload = {};
      if (req.filters !== undefined) {
        payload.filters = req.filters;
      }
      return executeActionJson<TagSearchResp>("tag", "tag_search", payload);
    },
  });
};

export const useTagGroupList = () => {
  return useQuery({
    queryKey: ["action", "tag", "tag_group_list"],
    queryFn: () =>
      executeActionJson<TagGroupListResp>("tag", "tag_group_list", {}),
  });
};

function useTagGroupMutation(actionKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { tagGroupId: string; value: string }) =>
      executeActionJson("tag", actionKey, {
        tagGroupRef: "id:" + props.tagGroupId,
        value: props.value,
      }),
    onSuccess: () => queryClient.invalidateQueries(),
  });
}

function useTagGlobalMutation(actionKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { tagId: string; value: string }) =>
      executeActionJson("tag", actionKey, {
        tagRef: "id:" + props.tagId,
        value: props.value,
      }),
    onSuccess: () => queryClient.invalidateQueries(),
  });
}

function useTagLocalMutation(actionKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { tagId: string; value: string }) =>
      executeActionJson("tag", actionKey, {
        tagRef: "id:" + props.tagId,
        value: props.value,
      }),
    onSuccess: () => queryClient.invalidateQueries(),
  });
}

export const useTagGroupUpdateName = () => {
  return useTagGroupMutation("tag_group_update_name");
};

export const useTagGroupUpdateDescription = () => {
  return useTagGroupMutation("tag_group_update_description");
};

export const useTagGroupUpdateKey = () => {
  return useTagGroupMutation("tag_group_update_key");
};

export const useTagGlobalUpdateName = () => {
  return useTagGlobalMutation("tag_global_update_name");
};

export const useTagGlobalUpdateDescription = () => {
  return useTagGlobalMutation("tag_global_update_description");
};

export const useTagLocalUpdateName = () => {
  return useTagLocalMutation("tag_local_update_name");
};

export const useTagLocalUpdateDescription = () => {
  return useTagLocalMutation("tag_local_update_description");
};
