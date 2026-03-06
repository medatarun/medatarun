export interface SecurityRuleDescriptionDto {
  key: string;
  name: string;
  description: string;
}

export interface SecurityRulesDescriptionsResp {
  items: SecurityRuleDescriptionDto[];
}
