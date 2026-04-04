import type {
  ActionDisplayedSubject,
  ActionPerformerRequestParams,
} from "@/components/business/actions/ActionPerformer.tsx";
import { refid } from "@/business/action_runner";

export const createActionTemplateRoleList = (): ActionPerformerRequestParams => {
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
