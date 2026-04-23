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
  performAction: (req: ActionRequest) => string;
  confirmAction: (
    requestId: string,
    formData: ActionPerformerFormData,
  ) => Promise<ActionResp>;
  cancelAction: (requestId: string, reason?: unknown) => void;
  finishAction: (requestId: string) => void;
}

export const ActionPerformerContext =
  createContext<ActionPerformerContextValue | null>(null);
