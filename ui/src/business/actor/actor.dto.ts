export interface WhoAmIRespDto {
  issuer: string;
  sub: string;
  fullname: string;
  admin: boolean;
  /** Identifiers of roles given to the actor */
  roles: string[];
  /** List of named permissions */
  permissions: string[];
}

export interface RoleInfoDto {
  id: string;
  key: string;
  name: string;
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
