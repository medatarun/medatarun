import type { SecurityRuleDescriptionDto } from "./config.dto.ts";

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
