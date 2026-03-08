import { useEffect, useMemo, useState } from "react";
import {
  ActionPerformer,
  type ActionPerformerState,
} from "./ActionPerformer.tsx";
import {
  ActionPerformerContext,
  type ActionPerformerContextValue,
} from "./ActionPerformerContext.tsx";
import { useActionRegistry } from "@/business/action_registry";
import type { ActionPostHooks } from "./ActionPostHook.ts";

export function ActionProvider({
  children,
  postHooks,
}: {
  children: React.ReactNode;
  postHooks: ActionPostHooks;
}) {
  const actionRegistry = useActionRegistry();
  const performer = useMemo(
    () => new ActionPerformer(actionRegistry, postHooks),
    [actionRegistry, postHooks],
  );
  const [state, setState] = useState<ActionPerformerState>(
    performer.getState(),
  );

  useEffect(() => {
    return performer.subscribe(setState);
  }, [performer]);

  const value: ActionPerformerContextValue = {
    state: state,
    postHooks: postHooks,
    performAction: (actionRequest) => performer.performAction(actionRequest),
    confirmAction: (payload) => performer.confirmAction(payload),
    cancelAction: (reason) => performer.cancelAction(reason),
    finishAction: () => performer.finishAction(),
  };

  return (
    <ActionPerformerContext.Provider value={value}>
      {children}
    </ActionPerformerContext.Provider>
  );
}
