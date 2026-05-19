import { useMemo } from "react";
import {
  SecurityPermissionRegistry,
  SecurityRuleDescriptionRegistry,
} from "@medatarun/ui/business/config";
import {
  useSecurityPermissions,
  useSecurityRuleDescriptions,
} from "./config.storage.ts";

export const useSecurityRuleDescriptionRegistry = () => {
  const { data } = useSecurityRuleDescriptions();
  const registry = useMemo(
    () => new SecurityRuleDescriptionRegistry(data ?? []),
    [data],
  );
  return { registry: registry };
};

export const usePermissionRegistry = () => {
  const { data } = useSecurityPermissions();
  const registry = useMemo(
    () => new SecurityPermissionRegistry(data ?? []),
    [data],
  );
  return { registry: registry };
};
