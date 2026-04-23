import { createContext } from "react";
import type {
  ActionPerformerFormData,
  ActionPerformerRequest,
  ActionPerformerState,
  ActionPostHooks,
  ActionResp,
} from "@/business/action-performer";

export interface ActionPerformerContextValue {
  state: ActionPerformerState;
  postHooks: ActionPostHooks;
  performAction: (req: ActionPerformerRequest) => void;
  confirmAction: (formData: ActionPerformerFormData) => Promise<ActionResp>;
  cancelAction: (reason?: unknown) => void;
  finishAction: () => void;
}

export const ActionPerformerContext =
  createContext<ActionPerformerContextValue | null>(null);
