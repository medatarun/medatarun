import {
  type ActionPayload,
  type ActionResp,
  executeAction,
} from "@/business/action_runner";
import { ActionRegistry } from "@/business/action_registry";
import { queryClient } from "@/services/queryClient.ts";
import type { ActionPostHooks } from "./ActionPostHook.ts";

export type ActionPerformerRequestParam = {
  readonly: boolean;
  value: unknown;
};
export type ActionPerformerRequestParams = Record<
  string,
  ActionPerformerRequestParam
>;

/**
 * Subject currently displayed by the UI where the action is triggered.
 *
 * Contract:
 * - This is page-level context, not row-level or transient item context.
 * - refs keys must follow action naming conventions (modelRef, entityRef, ...).
 * - refs values should use the same normalized format as action params ("id:...").
 */
export type ActionDisplayedSubjectNone = { kind: "none" };
export type ActionDisplayedSubjectResource = {
  kind: "resource";
  type: string;
  refs: Record<string, string>;
};
export type ActionDisplayedSubject =
  | ActionDisplayedSubjectNone
  | ActionDisplayedSubjectResource;
export type ActionPerformerFormData = Record<string, unknown>;

/**
 * Action execution request captured at trigger time.
 *
 * Contract:
 * - params are action input values.
 * - displayedSubject describes where the action was launched from.
 *   Post-action navigation relies on it for delete behaviors.
 */
export type ActionPerformerRequest = {
  actionGroupKey: string;
  actionKey: string;
  params: ActionPerformerRequestParams;
  displayedSubject: ActionDisplayedSubject;
};

export type ActionPerformerState =
  | { kind: "idle" }
  | { kind: "pendingUser"; request: ActionPerformerRequest }
  | { kind: "running"; request: ActionPerformerRequest }
  | { kind: "done"; request: ActionPerformerRequest }
  | { kind: "error"; request: ActionPerformerRequest; error: unknown };

type Listener = (s: ActionPerformerState) => void;

export class ActionPerformer {
  private actionRegistry: ActionRegistry;
  private postHooks: ActionPostHooks;
  private state: ActionPerformerState = { kind: "idle" };
  private listeners = new Set<Listener>();

  constructor(actionRegistry: ActionRegistry, postHooks: ActionPostHooks) {
    this.actionRegistry = actionRegistry;
    this.postHooks = postHooks;
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

  performAction(request: ActionPerformerRequest) {
    if (this.state.kind !== "idle") {
      throw new Error("Une action est déjà en cours");
    }
    this.setState({ kind: "pendingUser", request });
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
    const resp = await executeAction(actionGroupKey, actionKey, payload);
    const action = this.actionRegistry.findAction(actionGroupKey, actionKey);
    const request = this.state.kind === "running" ? this.state.request : null;
    let cachesHandled = false;
    if (request) {
      cachesHandled = await this.postHooks.onActionSuccess(
        {
          action: action,
          request: request,
          state: this.state,
        },
        queryClient,
      );
    }
    if (!cachesHandled) {
      await queryClient.invalidateQueries();
    }
    return resp;
  }
}
export const displaySubjectNone: ActionDisplayedSubjectNone = { kind: "none" };
