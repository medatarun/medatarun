import { type PropsWithChildren, useMemo } from "react";
import { type InlineEditCoordinator } from "./InlineEditCoordinator.tsx";
import { InlineEditCoordinatorDefault } from "./InlineEditCoordinatorDefault.ts";
import { InlineEditCoordinatorContext } from "./InlineEditCoordinator.context.ts";

/**
 * Provides the shared inline edit coordinator.
 *
 * The coordinator stores the currently open inline edit component. When another
 * inline edit component asks to open, the coordinator asks the current one to
 * cancel first.
 */
export function InlineEditCoordinatorProvider({ children }: PropsWithChildren) {
  const coordinator = useMemo<InlineEditCoordinator>(
    () => new InlineEditCoordinatorDefault(),
    [],
  );

  return (
    <InlineEditCoordinatorContext.Provider value={coordinator}>
      {children}
    </InlineEditCoordinatorContext.Provider>
  );
}
