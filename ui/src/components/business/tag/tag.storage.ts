import type {
  TagDto,
  TagGroupDto,
  TagScopeRef,
  TagSearchFilters,
} from "@/business/tag/tag.dto.ts";
import { useMutation, useQuery } from "@tanstack/react-query";
import { type ActionPayload } from "@/business/action-performer";
import { useActionPerformer } from "@/components/business/actions/action-performer-hook.tsx";

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
      return performer.executeJson<TagSearchResp>("tag", "tag_search", payload);
    },
  });
};

export const useTagGroupList = () => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["action", "tag", "tag_group_list"],
    queryFn: () =>
      performer.executeJson<TagGroupListResp>("tag", "tag_group_list", {}),
  });
};

function useTagGroupMutation(actionKey: string) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { tagGroupId: string; value: string }) =>
      performer.executeJson("tag", actionKey, {
        tagGroupRef: "id:" + props.tagGroupId,
        value: props.value,
      }),
  });
}

function useTagGlobalMutation(actionKey: string) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { tagId: string; value: string }) =>
      performer.executeJson("tag", actionKey, {
        tagRef: "id:" + props.tagId,
        value: props.value,
      }),
  });
}

function useTagLocalMutation(actionKey: string) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { tagId: string; value: string }) =>
      performer.executeJson("tag", actionKey, {
        tagRef: "id:" + props.tagId,
        value: props.value,
      }),
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
