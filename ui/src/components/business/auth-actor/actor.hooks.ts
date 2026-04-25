import { useRoleList, useWhoami } from "./actor.storage.ts";
import { useAuthentication } from "@seij/common-ui-auth";
import { useMemo } from "react";
import type { WhoAmIRespDto } from "@/business/actor/actor.dto.ts";
import { CurrentActor, RoleRegistry } from "@/business/actor";

const EMPTY_WHOAMI: WhoAmIRespDto = {
  admin: false,
  fullname: "",
  issuer: "",
  sub: "",
  permissions: [],
};
export const useCurrentActor = () => {
  const auth = useAuthentication();
  const { data } = useWhoami(auth.issuer, auth.subject);
  const currentActor = useMemo(() => {
    if (auth.isAuthenticated && data) return new CurrentActor(data, true);
    return new CurrentActor(EMPTY_WHOAMI, false);
  }, [auth.isAuthenticated, auth.issuer, auth.subject, data]);
  return currentActor;
};

export const useRoleRegistry = () => {
  const { data } = useRoleList();
  return useMemo(() => {
    if (data) {
      return new RoleRegistry(data.items);
    }
    return RoleRegistry.empty();
  }, [data]);
};
