import { createContext } from "react";
import {
  ActionPerformer,
  type ActionPerformerFormData,
  type ActionPerformerState,
  type ActionRequest,
  type ActionResp,
} from "@/business/action-performer";

export interface ActionPerformerContextValue {
  performer: ActionPerformer;
  state: ActionPerformerState;
  performAction: (req: ActionRequest) => void;
  confirmAction: (formData: ActionPerformerFormData) => Promise<ActionResp>;
  cancelAction: (reason?: unknown) => void;
  finishAction: () => void;
}

export const ActionPerformerContext =
  createContext<ActionPerformerContextValue | null>(null);
