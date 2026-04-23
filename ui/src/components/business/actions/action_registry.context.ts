import { createContext } from "react";
import { ActionRegistry } from "@/business/action_registry";

export const ActionRegistryContext = createContext<ActionRegistry | undefined>(
  undefined,
);
