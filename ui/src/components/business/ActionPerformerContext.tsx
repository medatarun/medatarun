import {createContext} from "react";
import type {ActionPerformerRequestParams, ActionPerformerState} from "./ActionPerformer.tsx";

export interface ActionPerformerContextValue {
  state: ActionPerformerState;
  performAction: (location: string, params: ActionPerformerRequestParams) => Promise<void>;
  confirmAction: (formData: ActionPerformerRequestParams) => Promise<void>;
  cancelAction: (reason?: unknown) => void;
  finishAction: () => void
};


export const ActionPerformerContext = createContext<ActionPerformerContextValue | null>(null);