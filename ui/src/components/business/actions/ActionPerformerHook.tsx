import {useContext} from "react";
import {ActionPerformerContext, type ActionPerformerContextValue} from "./ActionPerformerContext.tsx";

export function useActionPerformer(): ActionPerformerContextValue {
  const ctx = useContext(ActionPerformerContext);
  if (!ctx) throw new Error('useActionPerformer must be used in ActionProvider');
  return ctx;
}
