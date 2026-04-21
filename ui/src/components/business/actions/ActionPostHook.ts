import type { QueryClient } from "@tanstack/react-query";
import type { NavigateFn } from "@tanstack/react-router";
import type { ActionDescriptor } from "@/business/action_registry";
import type { ActionPerformerState } from "./ActionPerformer.tsx";
import type {
  ActionDisplayedSubject,
  ActionDisplayedSubjectResource,
  ActionPerformerRequest,
} from "./ActionPerformerRequest.tsx";

type ActionPostBaseContext = {
  action: ActionDescriptor;
  request: ActionPerformerRequest;
  state: ActionPerformerState;
};

export type ActionPostSuccessContext = {
  action: ActionDescriptor;
  request: ActionPerformerRequest;
  state: ActionPerformerState;
  displayedSubject: ActionDisplayedSubject;
};

export type ActionPostNavigationContext = ActionPostSuccessContext & {
  navigate: NavigateFn;
};

/**
 * Extension point executed after action success.
 *
 * Contract:
 * - ActionPerformer stays business-agnostic.
 * - Business modules (model/tag/...) implement their own hook.
 * - match(...) decides whether a hook applies for the displayed page subject.
 *
 * Call order for matching hooks:
 * 1) backend action execution succeeds
 * 2) onActionSuccess(...) is called
 * 3) generic cache fallback may run when no hook handled cache responsibility
 * 4) action state moves to done
 * 5) resolveNavigationAfterSuccess(...) is called to decide optional navigation
 */
export interface ActionPostHook {
  /**
   * Declares whether this hook is responsible for the current page subject.
   * The caller guarantees this is evaluated with the same displayedSubject
   * that was captured at action trigger time.
   */
  match: (subject: ActionDisplayedSubjectResource) => boolean;
  /**
   * Called after backend action execution succeeded, before the generic
   * `queryClient.invalidateQueries()` fallback is decided.
   *
   * Contract for implementers:
   * - Do cache updates/invalidation only for your business scope.
   * - Return true when this hook handled cache refresh responsibility.
   * - Return false when caller should keep the generic fallback invalidation.
   * - Throw on unexpected errors; caller will propagate the failure.
   *
   * Notes:
   * - In a multi-hook setup, several matching hooks can run for the same action.
   * - Returning true does not stop other matching hooks from running.
   */
  onActionSuccess: (
    context: ActionPostSuccessContext,
    queryClient: QueryClient,
  ) => Promise<boolean> | boolean;
  /**
   * Called after onActionSuccess(...) has completed and action state is done.
   * This method is an optional navigation side-effect handler.
   *
   * Contract for implementers:
   * - Call navigate(...) only when navigation is explicitly required.
   * - Do nothing to keep the current route.
   * - Do not perform cache work here (handled in onActionSuccess).
   */
  resolveNavigationAfterSuccess: (context: ActionPostNavigationContext) => void;
}

/**
 * Orchestrates multiple post hooks so callers do not reimplement list traversal.
 *
 * Contract:
 * - Only hooks matching the request.displayedSubject are executed.
 * - onActionSuccess returns true when at least one matching hook handled cache work.
 * - resolveNavigationAfterSuccess dispatches to every matching hook.
 */
export class ActionPostHooks {
  private readonly hooks: ActionPostHook[];

  constructor(hooks: ActionPostHook[]) {
    this.hooks = [...hooks];
  }

  async onActionSuccess(
    context: ActionPostBaseContext,
    queryClient: QueryClient,
  ): Promise<boolean> {
    const resolvedContext = this.toActionPostSuccessContext(context);
    let cachesHandled = false;
    for (const hook of this.hooks) {
      const hookHandledCaches = await hook.onActionSuccess(
        resolvedContext,
        queryClient,
      );
      cachesHandled = cachesHandled || hookHandledCaches;
    }
    return cachesHandled;
  }

  resolveNavigationAfterSuccess(
    context: ActionPostBaseContext & { navigate: NavigateFn },
  ): void {
    const resolvedContext = this.resolveNavigationContext(context);
    if (!resolvedContext) {
      return;
    }

    const displayedSubject = context.request.ctx.displayedSubject;
    if (displayedSubject.kind == "none") return;

    const matchingHooks = this.hooks.filter((it) => it.match(displayedSubject));
    for (const hook of matchingHooks) {
      hook.resolveNavigationAfterSuccess(resolvedContext);
    }
  }

  private toActionPostSuccessContext(
    context: ActionPostBaseContext,
  ): ActionPostSuccessContext {
    return {
      action: context.action,
      request: context.request,
      state: context.state,
      displayedSubject: context.request.ctx.displayedSubject,
    };
  }

  private resolveNavigationContext(
    context: ActionPostBaseContext & { navigate: NavigateFn },
  ): ActionPostNavigationContext | null {
    const successContext = this.toActionPostSuccessContext(context);
    if (!successContext) {
      return null;
    }
    return {
      ...successContext,
      navigate: context.navigate,
    };
  }
}
