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