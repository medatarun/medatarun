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

export type ActionPerformerState =
  | { kind: "idle" }
  | { kind: "pendingUser"; request: ActionRequest }
  | { kind: "running"; request: ActionRequest }
  | { kind: "done"; request: ActionRequest }
  | { kind: "error"; request: ActionRequest; error: unknown };

type Listener = (s: ActionPerformerState) => void;

export class ActionPerformer {
  private actionRegistry: ActionRegistry;
  private state: ActionPerformerState = { kind: "idle" };
  private listeners = new Set<Listener>();
  private queryClient: QueryClient;

  constructor(actionRegistry: ActionRegistry, queryClient: QueryClient) {
    this.actionRegistry = actionRegistry;
    this.queryClient = queryClient;
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

  performAction(request: ActionRequest) {
    if (this.state.kind !== "idle") {
      throw new Error("Une action est déjà en cours");
    }
    this.setState({ kind: "pendingUser", request });
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

  async confirmAction(payload: ActionPayload): Promise<ActionResp> {
    if (this.state.kind === "idle")
      throw Error("No pending or waiting request");
    if (this.state.kind === "running") throw Error("Request already running");
    if (this.state.kind === "done") throw Error("Request already finished");

    const { request } = this.state;
    this.setState({ kind: "running", request });

    try {
      const output: ActionResp = await this.execute(
        request.actionGroupKey,
        request.actionKey,
        payload,
      );
      this.setState({ kind: "done", request });
      return output;
    } catch (e) {
      this.setState({ kind: "error", request, error: e });
      throw e;
    }
  }

  cancelAction(reason?: unknown) {
    this.setState({ kind: "idle" });
  }

  finishAction() {
    this.setState({ kind: "idle" });
  }

  private async execute(
    actionGroupKey: string,
    actionKey: string,
    payload: ActionPayload,
  ): Promise<ActionResp> {
    const resp = await executeActionInternal(
      actionGroupKey,
      actionKey,
      payload,
    );
    const request = this.state.kind === "running" ? this.state.request : null;
    if (request) {
      await this.onActionSuccess(request);
    }
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
  resolveNavigationAfterSuccess(context: {
    request: ActionRequest;
    navigate: NavigateFn;
  }): void {
    const displayedSubject = context.request.ctx.displayedSubject;
    if (displayedSubject.kind == "none") return;
    actionPostNavigate({
      action:
        this.actionRegistry.findActionByActionKey(context.request.actionKey) ??
        throwError("Action not found in registry " + context.request.actionKey),
      request: context.request,
      navigate: context.navigate,
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
