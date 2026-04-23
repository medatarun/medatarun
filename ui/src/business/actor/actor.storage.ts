import { executeActionJson } from "@/business/action-performer";
import type {
  ActorDetailsDto,
  ActorInfoDto,
  RoleDetailsDto,
  RoleListDto,
  WhoAmIRespDto,
} from "@/business/actor/actor.dto.ts";
import { useMutation, useQuery } from "@tanstack/react-query";

/**
 * Returns the current user details with roles and permissions.
 *
 * Issuer and subject are used to manage the query cache, infos will only
 * be refetched if one of those changes.
 *
 * If one of "issuer" or "subject" is null, actor info will not be
 * retreived at all, preventing 401 errors on the query.
 *
 * Doing so, we prevent a bad loop where the user always gets
 * the "session expired" popup just after he just logged in.
 *
 */
export const useWhoami = (issuer: string | null, subject: string | null) => {
  return useQuery({
    queryKey: ["whoami", issuer, subject],
    queryFn: async () => {
      if (!issuer || !subject) return null;
      return executeActionJson<WhoAmIRespDto>("auth", "whoami", {});
    },
  });
};

export const useRoleList = () => {
  return useQuery({
    queryKey: ["action", "auth", "role", "list"],
    queryFn: () => executeActionJson<RoleListDto>("auth", "role_list", {}),
  });
};

export const useRole = (roleId: string) => {
  return useQuery({
    queryKey: ["action", "auth", "role", "get", roleId],
    enabled: roleId.length > 0,
    queryFn: () =>
      executeActionJson<RoleDetailsDto>("auth", "role_get", {
        roleRef: "id:" + roleId,
      }),
  });
};

export const useActorList = () => {
  return useQuery({
    queryKey: ["action", "auth", "actor", "list"],
    queryFn: () => executeActionJson<ActorInfoDto[]>("auth", "actor_list", {}),
  });
};

export const useActor = (actorId: string) => {
  return useQuery({
    queryKey: ["action", "auth", "actor", "get", actorId],
    enabled: actorId.length > 0,
    queryFn: () => {
      return executeActionJson<ActorDetailsDto>("auth", "actor_get", {
        actorId,
      });
    },
  });
};

function useRoleMutation(actionKey: string) {
  return useMutation({
    mutationFn: (props: { roleId: string; value: string }) =>
      executeActionJson("auth", actionKey, {
        roleRef: "id:" + props.roleId,
        value: props.value,
      }),
  });
}

export const useRoleUpdateName = () => {
  return useRoleMutation("role_update_name");
};

export const useRoleUpdateDescription = () => {
  return useRoleMutation("role_update_description");
};
