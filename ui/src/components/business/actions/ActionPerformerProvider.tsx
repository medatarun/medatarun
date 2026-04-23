import { useEffect, useState } from "react";
import { type ActionPerformerState } from "@/business/action-performer";
import {
  ActionPerformerContext,
  type ActionPerformerContextValue,
} from "./ActionPerformerContext.tsx";
import { ActionPerformerInstance } from "@/business/action-performer/action-performer-factory.ts";

export function ActionProvider({ children }: { children: React.ReactNode }) {
  const performer = ActionPerformerInstance;
  const [state, setState] = useState<ActionPerformerState>(
    performer.getState(),
  );

  useEffect(() => {
    return performer.subscribe(setState);
  }, [performer]);

  const value: ActionPerformerContextValue = {
    state: state,
    performer: performer,
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
