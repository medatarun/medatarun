import { useQuery } from "@tanstack/react-query";
import type { UserListDto } from "@/business/auth_user/user.dto.ts";
import { executeActionJson } from "@/business/action-performer";

export const ACTION_AUTH_QUERY_KEY_USER_LIST = [
  "action",
  "auth",
  "user",
  "list",
];

export const useUserList = () => {
  return useQuery({
    queryKey: ACTION_AUTH_QUERY_KEY_USER_LIST,
    queryFn: () => executeActionJson<UserListDto>("auth", "user_list", {}),
  });
};
