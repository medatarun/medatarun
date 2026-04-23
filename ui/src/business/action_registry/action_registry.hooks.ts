import { useContext } from "react";
import type { ActionRegistry } from "./action_registry.biz.tsx";
import { ActionRegistryContext } from "./action_registry.context.ts";

export const useActionRegistry = (): ActionRegistry => {
  const actions = useContext(ActionRegistryContext);
  if (!actions)
    throw new Error("useActions must be used within ActionsContext");
  return actions;
};
