import { createContext } from "react";
import type { ActionPerformerInputRegistry } from "./inputs/ActionPerformerInputRegistry.ts";

export const ActionPerformerInputRegistryContext = createContext<
  ActionPerformerInputRegistry | undefined
>(undefined);
