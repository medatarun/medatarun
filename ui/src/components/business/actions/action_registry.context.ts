import { createContext } from "react";
import { ActionRegistry } from "../../../business/action-registry";

export const ActionRegistryContext = createContext<ActionRegistry | undefined>(
  undefined,
);
