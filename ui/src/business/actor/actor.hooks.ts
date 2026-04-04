import { useWhoami } from "./actor.storage.ts";
import { useAuthentication } from "@seij/common-ui-auth";
import { useMemo } from "react";
import { CurrentActor } from "./actor.business.ts";
import type { WhoAmIRespDto } from "@/business/actor/actor.dto.ts";

const EMPTY_WHOAMI: WhoAmIRespDto = {
  admin: false,
  fullname: "",
  issuer: "",
  sub: "",
  roles: [],
  permissions: [],
};
export const useCurrentActor = () => {
  const auth = useAuthentication();
  const { data } = useWhoami(auth.issuer, auth.subject);
  const currentActor = useMemo(() => {
    if (auth.isAuthenticated && data) return new CurrentActor(data);
    return new CurrentActor(EMPTY_WHOAMI);
  }, [auth.isAuthenticated, auth.issuer, auth.subject, data]);
  return currentActor;
};
