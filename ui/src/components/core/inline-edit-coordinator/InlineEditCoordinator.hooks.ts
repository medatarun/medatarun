import { useContext, useRef } from "react";
import { InlineEditCoordinatorContext } from "./InlineEditCoordinator.context.ts";
import type { InlineEditIdentifier } from "./InlineEditCoordinator.tsx";

/**
 * Provides the current instance of InlineEditCoordinator
 * in this React context
 */
export function useInlineEditCoordinator() {
  const coordinator = useContext(InlineEditCoordinatorContext);
  if (coordinator == null) {
    throw new Error(
      "useInlineEditCoordinator must be used in InlineEditCoordinatorProvider",
    );
  }

  return coordinator;
}

/**
 * Creates a new identifier for an inline edit component.
 * Identifier is stable while the component is mounted.
 */
export function useInlineEditIdentifier() {
  return useRef<InlineEditIdentifier>({});
}
