import type { ActionCtx } from "./action-ctx.ts";

/**
 * Action execution request captured at trigger time.
 *
 * This is like an event sent by a page to say "user wants to do that;
 * with the context."
 */
export type ActionRequest = {
  /**
   * Action group
   */
  actionGroupKey: string;
  /**
   * Action to launch
   */
  actionKey: string;
  /**
   * Contextual information for the action (prefilled parameters, where we
   * are on screens, etc.)
   */
  ctx: ActionCtx;
};
