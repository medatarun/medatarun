export interface SecurityRuleDescriptionDto {
  key: string;
  name: string;
  description: string;
  associatedRequiredPermissions: string[];
}

export interface SecurityRulesDescriptionsResp {
  items: SecurityRuleDescriptionDto[];
}

export interface SecurityPermissionDto {
  id: string;
  name: string | null;
  description: string | null;
  implies: string[];
}

export interface SecurityPermissionsResp {
  items: SecurityPermissionDto[];
}
