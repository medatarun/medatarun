import { useContext } from "react";
import { ActionPerformerInputRegistryContext } from "./action-performer-input-registry-context.ts";

export function useActionPerformerInputRegistry() {
  const actionPerformerRegistry = useContext(
    ActionPerformerInputRegistryContext,
  );
  if (!actionPerformerRegistry) {
    throw new Error(
      "useActionPerformerInputRegistry must be used within ActionPerformerInputRegistryContext",
    );
  }
  return { actionPerformerRegistry };
}
