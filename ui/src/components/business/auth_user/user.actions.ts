import type {
  ActionDisplayedSubject,
  ActionPerformerRequestParam,
  ActionPerformerRequestParams,
} from "@/components/business/actions/ActionPerformer.tsx";

export const createActionTemplateUserList = (): ActionPerformerRequestParams => {
  return {};
};

export const createActionTemplateUser = (
  username: string,
): ActionPerformerRequestParams => {
  return {
    username: usernameParam(username),
  };
};

export const createDisplayedSubjectUser = (
  username: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "user",
  refs: { username },
});

function usernameParam(username: string): ActionPerformerRequestParam {
  return {
    value: username,
    readonly: true,
  };
}
