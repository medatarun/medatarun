import type {
  TagDto,
  TagGroupDto,
  TagScopeRef,
  TagSearchFilters,
} from "@/business/tag/tag.dto.ts";
import { useMutation, useQuery } from "@tanstack/react-query";
import { type ActionPayload } from "@/business/action-performer";
import { useActionPerformer } from "@/components/business/actions/action-performer-hook.tsx";
import type { ActionKey } from "../../../business/action-registry";

export type TagSearchReq = {
  filters?: TagSearchFilters | null;
};

export type TagSearchResp = {
  items: TagDto[];
};

export type TagGroupListResp = {
  items: TagGroupDto[];
};

export const useTagSearch = (req: TagSearchReq, enabled: boolean = true) => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["action", "tag", "tag_search", req.filters ?? null],
    enabled: enabled,
    queryFn: async () => {
      const payload: ActionPayload = {};
      if (req.filters !== undefined) {
        payload.filters = req.filters;
      }
      return performer.executeJson<TagSearchResp>("tags/tag_search", payload);
    },
  });
};

export const useTagGroupList = () => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["action", "tag", "tag_group_list"],
    queryFn: () =>
      performer.executeJson<TagGroupListResp>("tags/tag_group_list", {}),
  });
};

function useTagGroupMutation(actionRef: ActionKey) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { tagGroupId: string; value: string }) =>
      performer.executeJson(actionRef, {
        tagGroupRef: "id:" + props.tagGroupId,
        value: props.value,
      }),
  });
}

function useTagGlobalMutation(actionRef: ActionKey) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { tagId: string; value: string }) =>
      performer.executeJson(actionRef, {
        tagRef: "id:" + props.tagId,
        value: props.value,
      }),
  });
}

function useTagLocalMutation(actionRef: ActionKey) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { tagId: string; value: string }) =>
      performer.executeJson(actionRef, {
        tagRef: "id:" + props.tagId,
        value: props.value,
      }),
  });
}

export const useTagGroupUpdateName = () => {
  return useTagGroupMutation("tags/tag_group_update_name");
};

export const useTagGroupUpdateDescription = () => {
  return useTagGroupMutation("tags/tag_group_update_description");
};

export const useTagGroupUpdateKey = () => {
  return useTagGroupMutation("tags/tag_group_update_key");
};

export const useTagGlobalUpdateName = () => {
  return useTagGlobalMutation("tags/tag_global_update_name");
};

export const useTagGlobalUpdateDescription = () => {
  return useTagGlobalMutation("tags/tag_global_update_description");
};

export const useTagLocalUpdateName = () => {
  return useTagLocalMutation("tags/tag_local_update_name");
};

export const useTagLocalUpdateDescription = () => {
  return useTagLocalMutation("tags/tag_local_update_description");
};
