import { executeActionJson } from "@/business/action_runner";
import type { UserListDto } from "@/business/auth_user/user.dto.ts";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

async function userList() {
  return executeActionJson<UserListDto>("auth", "user_list", {});
}

async function userCreate(props: {
  username: string;
  fullname: string;
  password: string;
  admin: boolean;
}) {
  return executeActionJson("auth", "user_create", {
    username: props.username,
    fullname: props.fullname,
    password: props.password,
    admin: props.admin,
  });
}

async function userChangeFullname(props: {
  username: string;
  fullname: string;
}) {
  return executeActionJson("auth", "user_change_fullname", {
    username: props.username,
    fullname: props.fullname,
  });
}

async function userEnable(props: { username: string }) {
  return executeActionJson("auth", "user_enable", {
    username: props.username,
  });
}

async function userDisable(props: { username: string }) {
  return executeActionJson("auth", "user_disable", {
    username: props.username,
  });
}

async function userChangePassword(props: {
  username: string;
  password: string;
}) {
  return executeActionJson("auth", "user_change_password", {
    username: props.username,
    password: props.password,
  });
}

export const useUserList = () => {
  return useQuery({
    queryKey: ["action", "auth", "user_list"],
    queryFn: userList,
  });
};

export const useUserCreate = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: userCreate,
    onSuccess: () => queryClient.invalidateQueries(),
  });
};

export const useUserChangeFullname = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: userChangeFullname,
    onSuccess: () => queryClient.invalidateQueries(),
  });
};

export const useUserEnable = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: userEnable,
    onSuccess: () => queryClient.invalidateQueries(),
  });
};

export const useUserDisable = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: userDisable,
    onSuccess: () => queryClient.invalidateQueries(),
  });
};

export const useUserChangePassword = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: userChangePassword,
    onSuccess: () => queryClient.invalidateQueries(),
  });
};
