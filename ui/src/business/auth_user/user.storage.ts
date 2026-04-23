import { executeActionJson } from "@/business/action_runner";
import type { UserListDto } from "@/business/auth_user/user.dto.ts";
import { useQuery } from "@tanstack/react-query";

async function userList() {
  return executeActionJson<UserListDto>("auth", "user_list", {});
}

export const ACTION_AUTH_QUERY_KEY_USER_LIST = [
  "action",
  "auth",
  "user",
  "list",
];

export const useUserList = () => {
  return useQuery({
    queryKey: ACTION_AUTH_QUERY_KEY_USER_LIST,
    queryFn: userList,
  });
};
