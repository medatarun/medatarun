import type {
  SecurityPermissionsResp,
  SecurityRulesDescriptionsResp,
} from "./config.dto.ts";
import { useQuery } from "@tanstack/react-query";
import { useActionPerformer } from "@/components/business/actions/action-performer-hook.tsx";

export const useSecurityRuleDescriptions = () => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["action", "config", "inspect_security_rules"],
    queryFn: async () =>
      (
        await performer.executeJson<SecurityRulesDescriptionsResp>(
          "config",
          "inspect_security_rules",
          {},
        )
      ).items,
  });
};

export const useSecurityPermissions = () => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["action", "config", "inspect_permissions"],
    queryFn: async () =>
      (
        await performer.executeJson<SecurityPermissionsResp>(
          "config",
          "inspect_permissions",
          {},
        )
      ).items,
  });
};
