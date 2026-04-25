import { ActorInfo, AuthRole } from "@/business/actor";
import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/business/action-performer";

export const createDisplayedSubjectRole = (
  roleId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "role",
  refs: { roleId },
});

export const createDisplayedSubjectActor = (
  actorId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "actor",
  refs: { actorId },
});

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

export const createActionCtxRole = (
  role: AuthRole,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "roleRef",
        readonly: true,
        visible: false,
        defaultValue: () => "id:" + role.id,
      },
      {
        actionKey: "role_update_key",
        actionParamKey: "value",
        readonly: false,
        visible: true,
        defaultValue: () => role.key,
      },
      {
        actionKey: "role_update_autoassign",
        actionParamKey: "value",
        readonly: false,
        visible: true,
        defaultValue: () => role.autoAssign,
      },
    ],
    displayedSubject,
  );
};
export const createActionCtxRolePermission = (
  role: AuthRole,
  permissionKey: string,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "roleRef",
        readonly: true,
        visible: false,
        defaultValue: () => "id:" + role.id,
      },
      {
        actionParamKey: "permissionKey",
        readonly: true,
        visible: true,
        defaultValue: () => permissionKey,
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
