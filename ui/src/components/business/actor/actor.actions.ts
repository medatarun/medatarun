import type {
  ActionDisplayedSubject,
  ActionPerformerRequestParam,
  ActionPerformerRequestParams,
} from "@/components/business/actions";
import { refid } from "@/business/action_runner";

export const createActionTemplateRoleList =
  (): ActionPerformerRequestParams => {
    return {};
  };

export const createActionTemplateRole = (
  roleId: string,
): ActionPerformerRequestParams => {
  return {
    roleRef: refid(roleId),
  };
};

export const createDisplayedSubjectRole = (
  roleId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "role",
  refs: { roleId },
});

export const createDisplayedSubjectRolePermission = (
  roleId: string,
  permissionKey: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "role",
  refs: { roleId, permissionKey },
});

export const createActionTemplateActor = (
  actorId: string,
): ActionPerformerRequestParams => {
  return {
    actorId: actorIdParam(actorId),
  };
};

export const createDisplayedSubjectActor = (
  actorId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "actor",
  refs: { actorId },
});

export const createActionTemplateActorRoleList = (
  actorId: string,
): ActionPerformerRequestParams => {
  return {
    actorId: actorIdParam(actorId),
  };
};

export const createActionTemplateActorRole = (
  actorId: string,
  roleId: string,
): ActionPerformerRequestParams => {
  return {
    actorId: actorIdParam(actorId),
    roleRef: refid(roleId),
  };
};

export const createDisplayedSubjectActorRole = (
  actorId: string,
  roleId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "actor",
  refs: { actorId, roleId },
});

function actorIdParam(actorId: string): ActionPerformerRequestParam {
  return {
    value: actorId,
    readonly: true,
    visible: false,
  };
}
