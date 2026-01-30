import {createContext} from "react";
import type {ActionPerformerFormData, ActionPerformerRequest, ActionPerformerState} from "./ActionPerformer.tsx";
import type {ActionResp} from "../../business";

export interface ActionPerformerContextValue {
  state: ActionPerformerState;
  performAction: (req:ActionPerformerRequest) => void
  confirmAction: (formData: ActionPerformerFormData) => Promise<ActionResp>;
  cancelAction: (reason?: unknown) => void;
  finishAction: () => void
}


export const ActionPerformerContext = createContext<ActionPerformerContextValue | null>(null);