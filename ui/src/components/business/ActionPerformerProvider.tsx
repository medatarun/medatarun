import {useEffect, useMemo, useState,} from 'react';
import {ActionPerformer, type ActionPerformerState} from './ActionPerformer';
import {ActionPerformerContext, type ActionPerformerContextValue} from "./ActionPerformerContext.tsx";
import {useActionRegistry} from "../../business";


export function ActionProvider({children}: { children: React.ReactNode }) {
  const actionRegistry = useActionRegistry()
  const performer = useMemo(() => new ActionPerformer(actionRegistry), [actionRegistry]);
  const [state, setState] = useState<ActionPerformerState>(performer.getState());

  useEffect(() => {
    return performer.subscribe(setState);
  }, [performer]);

  const value: ActionPerformerContextValue = {
    state:state,
    performAction: (actionRequest) =>
      performer.performAction(actionRequest),
    confirmAction: (payload) => performer.confirmAction(payload),
    cancelAction: (reason) => performer.cancelAction(reason),
    finishAction: () => performer.finishAction()
  };

  return (
    <ActionPerformerContext.Provider value={value}>
      {children}
    </ActionPerformerContext.Provider>
  );
}
