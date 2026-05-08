import type { ActionCtx } from "./action-ctx.ts";
import type { ActionKey } from "@/business/action_registry";

/**
 * Action execution request captured at trigger time.
 *
 * This is like an event sent by a page to say "user wants to do that;
 * with the context."
 */
export type ActionRequest = {
  /**
   * Action
   */
  actionRef: ActionKey;
  /**
   * Contextual information for the action (prefilled parameters, where we
   * are on screens, etc.)
   */
  ctx: ActionCtx;
};
