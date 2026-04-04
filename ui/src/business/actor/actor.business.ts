import type { RoleDetailsDto, RoleInfoDto, WhoAmIRespDto } from "@/business/actor/actor.dto.ts";

export class CurrentActor {
  private readonly dto: WhoAmIRespDto;

  constructor(dto: WhoAmIRespDto) {
    this.dto = dto;
  }
  isAdmin() {
    return this.dto.admin
  }
}

export class AuthRole {
  readonly id: string;
  readonly key: string;
  readonly name: string;
  readonly description: string | null;
  readonly createdAt: string;
  readonly lastUpdatedAt: string;

  constructor(dto: RoleInfoDto) {
    this.id = dto.id;
    this.key = dto.key;
    this.name = dto.name;
    this.description = dto.description;
    this.createdAt = dto.createdAt;
    this.lastUpdatedAt = dto.lastUpdatedAt;
  }

  get label() {
    return this.name || this.key;
  }
}

export class AuthRoleDetails {
  readonly role: AuthRole;
  readonly permissions: string[];

  constructor(dto: RoleDetailsDto) {
    this.role = new AuthRole(dto.role);
    this.permissions = dto.permissions;
  }
}
