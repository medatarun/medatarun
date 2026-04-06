export interface UserInfoDto {
  id: string;
  username: string;
  fullname: string;
  admin: boolean;
  disabledDate: string | null;
}

export interface UserListDto {
  items: UserInfoDto[];
}
