import type {
  ActorDetailsDto,
  ActorInfoDto,
  RoleDetailsDto,
  RoleInfoDto,
  WhoAmIRespDto,
} from "@/business/actor/actor.dto.ts";
import { sortBy } from "lodash-es";

export class CurrentActor {
  private readonly dto: WhoAmIRespDto;

  constructor(dto: WhoAmIRespDto) {
    this.dto = dto;
  }
  isAdmin() {
    return this.dto.admin;
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

export class RoleRegistry {
  private readonly items: RoleInfoDto[];
  private readonly roleById: Map<string, RoleInfoDto>;

  constructor(items: RoleInfoDto[]) {
    this.items = items;
    this.roleById = new Map(items.map((role) => [role.id, role]));
  }

  static empty() {
    return new RoleRegistry([]);
  }

  findById(roleId: string) {
    return this.roleById.get(roleId);
  }

  /**
   * Returns only roles that are known in the registry, sorted by display label.
   */
  searchRolesByIdsSorted(roleIds: string[]) {
    const roles = roleIds
      .map((roleId) => this.findById(roleId))
      .filter((role): role is RoleInfoDto => role !== undefined)
      .map((role) => new AuthRole(role));

    return sortBy(roles, (role) => role.label.toLowerCase());
  }

  /**
   * Returns every known role in the registry sorted by display label.
   */
  findAllRolesSorted() {
    return sortBy(
      this.items.map((role) => new AuthRole(role)),
      (role) => role.label.toLowerCase(),
    );
  }
}

export class ActorInfo {
  readonly id: string;

  constructor(dto: ActorInfoDto) {
    this.id = dto.id;
  }
}

export class ActorDetails {
  readonly id: string;
  readonly roles: string[];

  constructor(dto: ActorDetailsDto) {
    this.id = dto.id;
    this.roles = dto.roles;
  }
}
