import {createContext} from "react";
import type {ActionPerformerRequestParams, ActionPerformerState, ActionRequest} from "./ActionPerformer.tsx";
import type {ActionResp} from "../../business";

export interface ActionPerformerContextValue {
  state: ActionPerformerState;
  performAction: (req:ActionRequest) => Promise<ActionResp>;
  confirmAction: (formData: ActionPerformerRequestParams) => Promise<ActionResp>;
  cancelAction: (reason?: unknown) => void;
  finishAction: () => void
}


export const ActionPerformerContext = createContext<ActionPerformerContextValue | null>(null);