export interface SecurityRuleDescriptionDto {
  key: string;
  description: string;
}

export interface SecurityRulesDescriptionsResp {
  items: SecurityRuleDescriptionDto[];
}
