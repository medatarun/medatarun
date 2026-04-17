import { ActorInfo, AuthRole } from "@/business/actor";
import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/components/business/actions";

export const createActionCtxActor = (
  actor: ActorInfo,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "actorId",
        readonly: true,
        visible: false,
        defaultValue: () => actor.id,
      },
    ],
    displayedSubject,
  );
};
export const createActionCtxActorRole = (
  actor: ActorInfo,
  role: AuthRole,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "actorId",
        readonly: true,
        visible: false,
        defaultValue: () => actor.id,
      },
      {
        actionParamKey: "roleRef",
        readonly: true,
        visible: true,
        defaultValue: () => "id:" + role.id,
      },
    ],
    displayedSubject,
  );
};
