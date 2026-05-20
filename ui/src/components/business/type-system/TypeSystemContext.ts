import { createContext } from "react";
import type { TypeRegistry } from "@medatarun/ui/business/types/TypeRegistry.ts";

export const TypeSystemContext = createContext<TypeRegistry | undefined>(
  undefined,
);
