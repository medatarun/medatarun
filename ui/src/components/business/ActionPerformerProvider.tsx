import {useEffect, useMemo, useState,} from 'react';
import {ActionPerformer, type ActionPerformerState} from './ActionPerformer';
import {ActionPerformerContext, type ActionPerformerContextValue} from "./ActionPerformerContext.tsx";


export function ActionProvider({children}: { children: React.ReactNode }) {
  const performer = useMemo(() => new ActionPerformer(), []);
  const [state, setState] = useState<ActionPerformerState>(performer.getState());

  useEffect(() => {
    return performer.subscribe(setState);
  }, [performer]);

  const value: ActionPerformerContextValue = {
    state,
    performAction: (location, params) =>
      performer.performAction(location, params),
    confirmAction: (formData) => performer.confirmAction(formData),
    cancelAction: (reason) => performer.cancelAction(reason),
    finishAction: () => performer.finishAction()
  };

  return (
    <ActionPerformerContext.Provider value={value}>
      {children}
    </ActionPerformerContext.Provider>
  );
}
