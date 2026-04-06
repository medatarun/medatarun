import { executeActionJson } from "@/business/action_runner";
import type {
  ActorDetailsDto,
  ActorInfoDto,
  RoleDetailsDto,
  RoleListDto,
  WhoAmIRespDto,
} from "@/business/actor/actor.dto.ts";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

async function whoami() {
  return executeActionJson<WhoAmIRespDto>("auth", "whoami", {});
}

async function roleList() {
  return executeActionJson<RoleListDto>("auth", "role_list", {});
}

async function roleGet(roleId: string) {
  return executeActionJson<RoleDetailsDto>("auth", "role_get", {
    roleRef: "id:" + roleId,
  });
}

async function actorList() {
  return executeActionJson<ActorInfoDto[]>("auth", "actor_list", {});
}

async function actorGet(actorId: string) {
  return executeActionJson<ActorDetailsDto>("auth", "actor_get", { actorId });
}

export const useWhoami = (issuer: string | null, subject: string | null) => {
  return useQuery({
    queryKey: ["whoami", issuer, subject],
    queryFn: whoami,
  });
};

export const useRoleList = () => {
  return useQuery({
    queryKey: ["action", "auth", "role_list"],
    queryFn: roleList,
  });
};

export const useRole = (roleId: string) => {
  return useQuery({
    queryKey: ["action", "auth", "role_get", roleId],
    enabled: roleId.length > 0,
    queryFn: () => roleGet(roleId),
  });
};

export const useActorList = () => {
  return useQuery({
    queryKey: ["action", "auth", "actor_list"],
    queryFn: actorList,
  });
};

export const useActor = (actorId: string) => {
  return useQuery({
    queryKey: ["action", "auth", "actor_get", actorId],
    enabled: actorId.length > 0,
    queryFn: () => actorGet(actorId),
  });
};

function useRoleMutation(actionKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { roleId: string; value: string }) =>
      executeActionJson("auth", actionKey, {
        roleRef: "id:" + props.roleId,
        value: props.value,
      }),
    onSuccess: () => queryClient.invalidateQueries(),
  });
}

export const useRoleUpdateName = () => {
  return useRoleMutation("role_update_name");
};

export const useRoleUpdateDescription = () => {
  return useRoleMutation("role_update_description");
};
