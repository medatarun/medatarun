import type {
  SecurityPermissionDto,
  SecurityRuleDescriptionDto,
} from "./config.dto.ts";
import { includes } from "lodash-es";

export class SecurityRuleDescriptionRegistry {
  private readonly itemsByKey: Map<string, SecurityRuleDescriptionDto>;

  constructor(items: SecurityRuleDescriptionDto[]) {
    this.itemsByKey = new Map<string, SecurityRuleDescriptionDto>();
    items.forEach((item) => {
      this.itemsByKey.set(item.key, item);
    });
  }

  find(ruleKey: string): SecurityRuleDescriptionDto | undefined {
    return this.itemsByKey.get(ruleKey);
  }

  findDescription(ruleKey: string): string | undefined {
    return this.find(ruleKey)?.description;
  }

  findName(ruleKey: string): string | undefined {
    return this.find(ruleKey)?.name;
  }

  findNameOrDefault(ruleKey: string): string {
    return this.findName(ruleKey) ?? ruleKey;
  }
}

export class SecurityPermissionRegistry {
  private readonly itemsById: Map<string, SecurityPermission>;
  private readonly items: SecurityPermission[];

  constructor(items: SecurityPermissionDto[]) {
    this.items = items.map((item) => new SecurityPermission(item));
    this.itemsById = new Map<string, SecurityPermission>();
    this.items.forEach((item) => {
      this.itemsById.set(item.id, item);
    });
  }

  find(permissionKey: string): SecurityPermission | undefined {
    return this.itemsById.get(permissionKey);
  }

  findName(permissionKey: string): string | null | undefined {
    return this.find(permissionKey)?.name;
  }

  findDescription(permissionKey: string): string | null | undefined {
    return this.find(permissionKey)?.description;
  }

  findAll(): SecurityPermission[] {
    return [...this.items];
  }

  findImpliedPermissions(permissions: string[]): string[] {
    const collect: string[] = [];
    for (const pkey of permissions) {
      const impliedKeys = this.find(pkey)?.implies ?? [];
      for (const pImpliedKey of impliedKeys) {
        if (
          !includes(collect, pImpliedKey) &&
          !includes(permissions, pImpliedKey)
        ) {
          collect.push(pImpliedKey);
        }
      }
    }
    return collect;
  }
}

export class SecurityPermission {
  readonly id: string;
  readonly name: string | null;
  readonly description: string | null;
  readonly implies: string[];

  constructor(dto: SecurityPermissionDto) {
    this.id = dto.id;
    this.name = dto.name;
    this.description = dto.description;
    this.implies = dto.implies;
  }
}
