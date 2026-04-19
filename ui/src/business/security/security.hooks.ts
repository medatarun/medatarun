import { useCurrentActor } from "@/business/actor";
import { useSecurityRuleDescriptionRegistry } from "@/business/config";
import { useActionRegistry } from "@/business/action_registry";
import type { ActionKey } from "@/business/action_registry/actionRegistry.dictionnary.ts";

export function useSecurityContext() {
  const actor = useCurrentActor();
  const { registry } = useSecurityRuleDescriptionRegistry();
  const actionRegistry = useActionRegistry();

  const canExecute = (rule: string): boolean => {
    // Special rules
    if (rule === "admin") return actor.isAdmin();
    if (rule === "public") return true;
    if (rule === "signed_in") return actor.isSignedIn();

    // Let's see if the rule has associated permissions
    const permsAssociated =
      registry.find(rule)?.associatedRequiredPermissions ?? [];
    if (permsAssociated.length > 0) {
      // If some of them are declared, we conclude that user
      // satisfies the rule if he has all permissions indicated on the rule
      return permsAssociated.every((p) => actor.hasPermission(p));
    }

    // Finally, if we don't know how to interpret the rule, so it will be false
    return false;
  };

  const canExecuteAction = (actionKey: ActionKey): boolean => {
    const actionFound = actionRegistry.findActionByActionKey(actionKey);
    if (!actionFound) return false;
    const rule = actionFound.securityRule;
    return canExecute(rule);
  };
  const canExecuteActions = (...actionKey: ActionKey[]): boolean => {
    return actionKey.every((k) => canExecuteAction(k));
  };
  return {
    canExecute: canExecute,
    canExecuteAction: canExecuteAction,
    canExecuteActions: canExecuteActions,
  };
}
