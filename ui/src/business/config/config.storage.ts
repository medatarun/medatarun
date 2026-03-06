import { executeAction } from "@/business/action_runner";
import type {
  SecurityRuleDescriptionDto,
  SecurityRulesDescriptionsResp,
} from "./config.dto.ts";
import { useQuery } from "@tanstack/react-query";

export async function fetchSecurityRuleDescriptions(): Promise<
  SecurityRuleDescriptionDto[]
> {
  const response = await executeAction<SecurityRulesDescriptionsResp>(
    "config",
    "inspect_security_rules",
    {},
  );
  if (response.contentType !== "json") {
    return [];
  }
  return response.json.items;
}

export const useSecurityRuleDescriptions = () => {
  return useQuery({
    queryKey: ["action", "config", "inspect_security_rules"],
    queryFn: fetchSecurityRuleDescriptions,
  });
};
