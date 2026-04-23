import { type ActionKey, ActionRegistry } from "@/business/action_registry";
import type { ActionRequest } from "./action-request.ts";
import {
  executeActionInternal,
  executeActionJsonInternal,
} from "./action-execute-internal.ts";
import type { ActionPayload, ActionResp } from "./action-types.ts";
import type { QueryClient } from "@tanstack/react-query";
import { actionPostCacheManagement } from "@/business/action-performer/action-post-caches.ts";
import { actionPostNavigate } from "@/business/action-performer/action-post-navigate.ts";
import type { NavigateFn } from "@tanstack/react-router";
import { throwError } from "@seij/common-types";

export type ActionPerformerFormData = Record<string, unknown>;

export type ActionPerformerRequestState =
  | { requestId: string; kind: "pendingUser"; request: ActionRequest }
  | { requestId: string; kind: "running"; request: ActionRequest }
  | { requestId: string; kind: "done"; request: ActionRequest }
  | {
      requestId: string;
      kind: "error";
      request: ActionRequest;
      error: unknown;
    };

export type ActionPerformerState = {
  requests: ActionPerformerRequestState[];
};

type Listener = (s: ActionPerformerState) => void;

export class ActionPerformer {
  private actionRegistry: ActionRegistry;
  private state: ActionPerformerState = {
    requests: [],
  };
  private listeners = new Set<Listener>();
  private queryClient: QueryClient;
  private navigate: NavigateFn;
  private nextRequestSequence = 0;

  constructor(
    actionRegistry: ActionRegistry,
    queryClient: QueryClient,
    navigate: NavigateFn,
  ) {
    this.actionRegistry = actionRegistry;
    this.queryClient = queryClient;
    this.navigate = navigate;
  }

  subscribe(listener: Listener) {
    this.listeners.add(listener);
    listener(this.state); // push l'état courant tout de suite
    return () => {
      this.listeners.delete(listener);
    };
  }

  private setState(next: ActionPerformerState) {
    this.state = next;
    this.listeners.forEach((l) => l(next));
  }

  getState() {
    return this.state;
  }

  getRequestState(requestId: string): ActionPerformerRequestState | null {
    return findRequestById(this.state, requestId);
  }

  getLastStartedRequestState(): ActionPerformerRequestState | null {
    if (this.state.requests.length === 0) {
      return null;
    }
    return this.state.requests[this.state.requests.length - 1];
  }

  performAction(request: ActionRequest): string {
    const requestId = `req_${++this.nextRequestSequence}`;
    const requestState = createPendingRequestState(requestId, request);
    this.setState(appendRequest(this.state, requestState));
    return requestId;
  }

  executeAny<T = unknown>(
    actionGroup: string,
    actionName: string,
    payload: ActionPayload,
  ): Promise<ActionResp<T>> {
    return executeActionInternal(actionGroup, actionName, payload);
  }
  executeJson<T = unknown>(
    actionGroup: string,
    actionName: string,
    payload: ActionPayload,
  ): Promise<T> {
    return executeActionJsonInternal(actionGroup, actionName, payload);
  }

  async confirmAction(
    requestId: string,
    payload: ActionPayload,
  ): Promise<ActionResp> {
    const requestState = findRequestByIdOrThrow(this.state, requestId);
    if (requestState.kind === "running") {
      throw Error("Request already running");
    }
    if (requestState.kind === "done") {
      throw Error("Request already finished");
    }
    this.setState(setRequestRunningState(this.state, requestId));

    try {
      const output: ActionResp = await this.execute(
        requestState.request,
        payload,
      );
      this.setState(setRequestDoneState(this.state, requestId));
      return output;
    } catch (e) {
      this.setState(setRequestErrorState(this.state, requestId, e));
      throw e;
    }
  }

  cancelAction(requestId: string, reason?: unknown) {
    this.setState(removeRequestById(this.state, requestId));
  }

  finishAction(requestId: string) {
    this.setState(removeRequestById(this.state, requestId));
  }

  private async execute(
    request: ActionRequest,
    payload: ActionPayload,
  ): Promise<ActionResp> {
    const resp = await executeActionInternal(
      request.actionGroupKey,
      request.actionKey,
      payload,
    );
    await this.onActionSuccess(request);
    return resp;
  }

  /**
   * Called after onActionSuccess(...) has completed and action state is done.
   * This method is an optional navigation side-effect handler.
   *
   * Contract for implementers:
   * - Call navigate(...) only when navigation is explicitly required.
   * - Do nothing to keep the current route.
   * - Do not perform cache work here (handled in onActionSuccess).
   */
  resolveNavigationAfterSuccess(context: { request: ActionRequest }): void {
    const displayedSubject = context.request.ctx.displayedSubject;
    if (displayedSubject.kind == "none") return;
    actionPostNavigate({
      action:
        this.actionRegistry.findActionByActionKey(context.request.actionKey) ??
        throwError("Action not found in registry " + context.request.actionKey),
      request: context.request,
      navigate: this.navigate,
      displayedSubject: context.request.ctx.displayedSubject,
    });
  }

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
  private async onActionSuccess(request: ActionRequest) {
    return actionPostCacheManagement(
      request.actionKey as ActionKey,
      this.queryClient,
      this.actionRegistry,
    );
  }
}

function findRequestByIdOrThrow(
  state: ActionPerformerState,
  requestId: string,
): ActionPerformerRequestState {
  const request = findRequestById(state, requestId);
  if (request == null) {
    throw new Error(`Action request not found: ${requestId}`);
  }
  return request;
}

function findRequestById(
  state: ActionPerformerState,
  requestId: string,
): ActionPerformerRequestState | null {
  return state.requests.find((it) => it.requestId === requestId) ?? null;
}

function createPendingRequestState(
  requestId: string,
  request: ActionRequest,
): ActionPerformerRequestState {
  return {
    requestId,
    kind: "pendingUser",
    request,
  };
}

function appendRequest(
  state: ActionPerformerState,
  request: ActionPerformerRequestState,
): ActionPerformerState {
  return {
    requests: [...state.requests, request],
  };
}

function replaceRequestById(
  state: ActionPerformerState,
  nextRequest: ActionPerformerRequestState,
): ActionPerformerState {
  return {
    requests: state.requests.map((request) =>
      request.requestId === nextRequest.requestId ? nextRequest : request,
    ),
  };
}

function setRequestRunningState(
  state: ActionPerformerState,
  requestId: string,
): ActionPerformerState {
  const request = findRequestByIdOrThrow(state, requestId);
  return replaceRequestById(state, {
    requestId: request.requestId,
    kind: "running",
    request: request.request,
  });
}

function setRequestDoneState(
  state: ActionPerformerState,
  requestId: string,
): ActionPerformerState {
  const request = findRequestByIdOrThrow(state, requestId);
  return replaceRequestById(state, {
    requestId: request.requestId,
    kind: "done",
    request: request.request,
  });
}

function setRequestErrorState(
  state: ActionPerformerState,
  requestId: string,
  error: unknown,
): ActionPerformerState {
  const request = findRequestByIdOrThrow(state, requestId);
  return replaceRequestById(state, {
    requestId: request.requestId,
    kind: "error",
    request: request.request,
    error,
  });
}

function removeRequestById(
  state: ActionPerformerState,
  requestId: string,
): ActionPerformerState {
  return {
    requests: state.requests.filter(
      (request) => request.requestId !== requestId,
    ),
  };
}
