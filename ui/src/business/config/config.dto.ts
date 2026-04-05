export interface SecurityRuleDescriptionDto {
  key: string;
  name: string;
  description: string;
}

export interface SecurityRulesDescriptionsResp {
  items: SecurityRuleDescriptionDto[];
}

export interface SecurityPermissionDto {
  id: string;
  name: string | null;
  description: string | null;
}

export interface SecurityPermissionsResp {
  items: SecurityPermissionDto[];
}
