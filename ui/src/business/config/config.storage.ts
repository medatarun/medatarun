import { executeActionJson } from "@/business/action-performer";
import type {
  SecurityPermissionsResp,
  SecurityRulesDescriptionsResp,
} from "./config.dto.ts";
import { useQuery } from "@tanstack/react-query";

export const useSecurityRuleDescriptions = () => {
  return useQuery({
    queryKey: ["action", "config", "inspect_security_rules"],
    queryFn: async () =>
      (
        await executeActionJson<SecurityRulesDescriptionsResp>(
          "config",
          "inspect_security_rules",
          {},
        )
      ).items,
  });
};

export const useSecurityPermissions = () => {
  return useQuery({
    queryKey: ["action", "config", "inspect_permissions"],
    queryFn: async () =>
      (
        await executeActionJson<SecurityPermissionsResp>(
          "config",
          "inspect_permissions",
          {},
        )
      ).items,
  });
};
