import { useContext } from "react";
import { ActionRegistryContext } from "./action_registry.context.ts";
import { ActionRegistry } from "@/business/action_registry";

export const useActionRegistry = (): ActionRegistry => {
  const actions = useContext(ActionRegistryContext);
  if (!actions)
    throw new Error("useActions must be used within ActionsContext");
  return actions;
};
