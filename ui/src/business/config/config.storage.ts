import { executeActionJson } from "@/business/action-performer";
import type {
  SecurityPermissionDto,
  SecurityPermissionsResp,
  SecurityRuleDescriptionDto,
  SecurityRulesDescriptionsResp,
} from "./config.dto.ts";
import { useQuery } from "@tanstack/react-query";

export async function fetchSecurityRuleDescriptions(): Promise<
  SecurityRuleDescriptionDto[]
> {
  const response = await executeActionJson<SecurityRulesDescriptionsResp>(
    "config",
    "inspect_security_rules",
    {},
  );
  return response.items;
}

export const useSecurityRuleDescriptions = () => {
  return useQuery({
    queryKey: ["action", "config", "inspect_security_rules"],
    queryFn: fetchSecurityRuleDescriptions,
  });
};

export async function fetchSecurityPermissions(): Promise<
  SecurityPermissionDto[]
> {
  const response = await executeActionJson<SecurityPermissionsResp>(
    "config",
    "inspect_permissions",
    {},
  );
  return response.items;
}

export const useSecurityPermissions = () => {
  return useQuery({
    queryKey: ["action", "config", "inspect_permissions"],
    queryFn: fetchSecurityPermissions,
  });
};
