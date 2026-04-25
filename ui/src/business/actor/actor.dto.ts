export interface WhoAmIRespDto {
  issuer: string;
  sub: string;
  fullname: string;
  admin: boolean;
  /** List of named permissions */
  permissions: string[];
}

export interface RoleInfoDto {
  id: string;
  key: string;
  name: string;
  managedRole: boolean;
  autoAssign: boolean;
  description: string | null;
  createdAt: string;
  lastUpdatedAt: string;
}

export interface RoleListDto {
  items: RoleInfoDto[];
}

export interface RoleDetailsDto {
  role: RoleInfoDto;
  permissions: string[];
}

export interface ActorInfoDto {
  id: string;
  issuer: string;
  subject: string;
  fullname: string;
  email: string | null;
  disabledAt: string | null;
  createdAt: string;
  lastSeenAt: string;
}

export interface ActorDetailsDto {
  id: string;
  issuer: string;
  subject: string;
  fullname: string;
  email: string | null;
  roles: string[];
  permissions: string[];
  disabledAt: string | null;
  createdAt: string;
  lastSeenAt: string;
}
