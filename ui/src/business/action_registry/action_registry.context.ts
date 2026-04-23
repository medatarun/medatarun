import { createContext } from "react";
import type { ActionRegistry } from "./action_registry.biz.tsx";

export const ActionRegistryContext = createContext<ActionRegistry | undefined>(
  undefined,
);
