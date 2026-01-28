import {type ActionPayload, ActionRegistry, type ActionResp, executeAction} from "../../business";
import {queryClient} from "../../services/queryClient.ts";

export type ActionPerformerRequestParam = {
  readonly: boolean,
  value: unknown
};
export type ActionPerformerRequestParams = Record<string, ActionPerformerRequestParam>;
export type ActionPerformerFormData = Record<string, unknown>;
export type ActionPerformerRequest = {
  actionGroupKey: string;
  actionKey: string
  params: ActionPerformerRequestParams;
};

export type ActionPerformerState =
  | { kind: 'idle' }
  | { kind: 'pendingUser'; request: ActionPerformerRequest }
  | { kind: 'running'; request: ActionPerformerRequest }
  | { kind: 'done'; request: ActionPerformerRequest }
  | { kind: 'error'; request: ActionPerformerRequest; error: unknown };

type Listener = (s: ActionPerformerState) => void;

export class ActionPerformer {
  private actionRegistry: ActionRegistry
  private state: ActionPerformerState = {kind: 'idle'};
  private listeners = new Set<Listener>();

  constructor(actionRegistry: ActionRegistry) {
    this.actionRegistry = actionRegistry;
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
    this.listeners.forEach(l => l(next));
  }

  getState() {
    return this.state;
  }

  performAction(request: ActionPerformerRequest) {
    if (this.state.kind !== 'idle') {
      throw new Error('Une action est déjà en cours')
    }
    this.setState({kind: 'pendingUser', request});
  }

  async confirmAction(payload: ActionPayload): Promise<ActionResp> {
    if (this.state.kind === 'idle') throw Error("No pending or waiting request");
    if (this.state.kind === 'running') throw Error("Request already running");
    if (this.state.kind === 'done') throw Error("Request already finished");

    const {request} = this.state;
    this.setState({kind: 'running', request});

    try {
      const output: ActionResp = await this.execute(request.actionGroupKey, request.actionKey, payload);
      this.setState({kind: 'done', request});
      return output
    } catch (e) {
      this.setState({kind: 'error', request, error: e});
      throw e
    }

  }

  cancelAction(reason?: unknown) {
    this.setState({kind: 'idle'});
  }

  finishAction() {
    this.setState({kind: 'idle'});
  }

  private async execute(
    actionGroupKey: string,
    actionKey: string,
    payload: ActionPayload
  ): Promise<ActionResp> {

    const resp = await executeAction(actionGroupKey, actionKey, payload);
    await queryClient.invalidateQueries()
    return resp
  }
}

