import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/business/action-performer";
import type { UserInfoDto } from "@/business/auth_user/index.ts";

export const createDisplayedSubjectUser = (
  username: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "user",
  refs: { username },
});

export const createActionCtxUser = (
  user: UserInfoDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "username",
        readonly: true,
        visible: true,
        defaultValue: () => user.username,
      },
      {
        actionKey: "user_change_fullname",
        actionParamKey: "fullname",
        readonly: false,
        visible: true,
        defaultValue: () => user.fullname,
      },
    ],
    displayedSubject,
  );
};
