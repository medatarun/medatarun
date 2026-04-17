import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/components/business/actions";
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
        defaultValue: () => user.id,
      },
    ],
    displayedSubject,
  );
};
