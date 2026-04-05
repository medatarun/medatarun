import { executeActionJson } from "@/business/action_runner";
import type { RoleDetailsDto, RoleListDto, WhoAmIRespDto } from "@/business/actor/actor.dto.ts";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

async function whoami() {
  return executeActionJson<WhoAmIRespDto>("auth", "whoami", {})
}

async function roleList() {
  return executeActionJson<RoleListDto>("auth", "role_list", {});
}

async function roleGet(roleRef: string) {
  return executeActionJson<RoleDetailsDto>("auth", "role_get", { roleRef });
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

export const useRole = (roleRef: string) => {
  return useQuery({
    queryKey: ["action", "auth", "role_get", roleRef],
    enabled: roleRef.length > 0,
    queryFn: () => roleGet(roleRef),
  });
};

function useRoleMutation(actionKey: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { roleRef: string; value: string }) =>
      executeActionJson("auth", actionKey, {
        roleRef: props.roleRef,
        value: props.value,
      }),
    onSuccess: () => queryClient.invalidateQueries(),
  });
}

export const useRoleUpdateName = () => {
  return useRoleMutation("role_update_name");
};

export const useRoleUpdateKey = () => {
  return useRoleMutation("role_update_key");
};

export const useRoleUpdateDescription = () => {
  return useRoleMutation("role_update_description");
};
