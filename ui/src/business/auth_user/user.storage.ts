import { useQuery } from "@tanstack/react-query";
import type { UserListDto } from "@/business/auth_user/user.dto.ts";
import { useActionPerformer } from "@/components/business/actions/action-performer-hook.tsx";

export const ACTION_AUTH_QUERY_KEY_USER_LIST = [
  "action",
  "auth",
  "user",
  "list",
];

export const useUserList = () => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ACTION_AUTH_QUERY_KEY_USER_LIST,
    queryFn: () => performer.executeJson<UserListDto>("auth", "user_list", {}),
  });
};
