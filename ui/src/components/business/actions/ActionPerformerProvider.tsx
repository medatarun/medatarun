import { useEffect, useState } from "react";
import {
  ActionPerformer,
  type ActionPerformerState,
} from "@/business/action-performer";
import {
  ActionPerformerContext,
  type ActionPerformerContextValue,
} from "./ActionPerformerContext.tsx";

export function ActionPerformerProvider({
  performer,
  children,
}: {
  performer: ActionPerformer;
  children: React.ReactNode;
}) {
  const [state, setState] = useState<ActionPerformerState>(
    performer.getState(),
  );

  useEffect(() => {
    return performer.subscribe(setState);
  }, [performer]);

  const value: ActionPerformerContextValue = {
    state,
    performer,
    performAction: (actionRequest) => performer.performAction(actionRequest),
    confirmAction: (requestId, payload) =>
      performer.confirmAction(requestId, payload),
    cancelAction: (requestId, reason) =>
      performer.cancelAction(requestId, reason),
    finishAction: (requestId) => performer.finishAction(requestId),
  };

  return (
    <ActionPerformerContext.Provider value={value}>
      {children}
    </ActionPerformerContext.Provider>
  );
}
