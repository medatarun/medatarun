import type { InlineEditCoordinator } from "@medatarun/ui/components/core/inline-edit-coordinator/InlineEditCoordinator.tsx";
import { createContext } from "react";

/**
 * React context populated with a single instance of InlineEditCoordinator
 */
export const InlineEditCoordinatorContext =
  createContext<InlineEditCoordinator | null>(null);
