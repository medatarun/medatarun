import {
  type ActionDescriptor,
  type ActionKey,
  ActionRegistry,
} from "@medatarun/ui/business/action-registry";
import type { QueryClient } from "@tanstack/react-query";
import type { ActionRequest } from "@medatarun/ui/business/action-performer/action-request.ts";
import type { ActionDisplayedSubject } from "@medatarun/ui/business/action-performer/action-ctx-displayed-subject.ts";
import type { NavigateFn } from "@tanstack/react-router";

/**
 * Called after backend action execution succeeded to handle query caches.
 *
 * Look at action semantics and decide which queryClient caches must be
 * invalidated. For example, when a mutation occurs (item deleted, name
 * change, ...) The caches of matching subjects must be discared so the
 * screen can be refreshed.
 */
export type ActionPostCacheManagement = (
  actionKey: ActionKey,
  queryClient: QueryClient,
  actionRegistry: ActionRegistry,
) => void;

/**
 * Called after an action was successfully completed and the action state is done.
 * This acts as a navigation side effect handler.
 */
export type ActionPostNavigate = (context: {
  action: ActionDescriptor;
  request: ActionRequest;
  displayedSubject: ActionDisplayedSubject;
  navigate: NavigateFn;
}) => void;
