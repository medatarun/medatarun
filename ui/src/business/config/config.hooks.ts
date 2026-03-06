import {useMemo} from "react";
import {SecurityRuleDescriptionRegistry} from "./config.model.ts";
import {useSecurityRuleDescriptions} from "./config.storage.ts";

export const useSecurityRuleDescriptionRegistry = () => {
  const { data } = useSecurityRuleDescriptions();
  const registry = useMemo(
    () => new SecurityRuleDescriptionRegistry(data ?? []),
    [data],
  );
  return { registry: registry };
};
