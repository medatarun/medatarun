import { createContext } from "react";
import type {
  ActionPerformerFormData,
  ActionPerformerState,
} from "./ActionPerformer.tsx";
import { type ActionResp } from "@/business/action_runner";
import type { ActionPostHooks } from "./ActionPostHook.ts";
import type { ActionPerformerRequest } from "./ActionPerformerRequest.tsx";

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
