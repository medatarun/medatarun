import {ActionRegistry, type ActionResp, executeAction} from "../../business/actionDescriptor.tsx";

export type ActionPerformerRequestParams = Record<string, unknown>;
export type ActionRequest = {
  actionGroupKey: string;
  actionKey: string
  location: string;
  params: ActionPerformerRequestParams;
};

export type ActionPerformerState =
  | { kind: 'idle' }
  | { kind: 'pendingUser'; request: ActionRequest }
  | { kind: 'running'; request: ActionRequest }
  | { kind: 'done'; request: ActionRequest }
  | { kind: 'error'; request: ActionRequest; error: unknown };

type Listener = (s: ActionPerformerState) => void;

export class ActionPerformer {
  private actionRegistry: ActionRegistry
  private state: ActionPerformerState = {kind: 'idle'};
  private listeners = new Set<Listener>();

  // pour relier la Promise initiée par performAction
  private currentPromise:
    | { resolve: (result: ActionResp) => void; reject: (e: unknown) => void }
    | null = null;

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

  performAction(request: ActionRequest): Promise<ActionResp> {
    if (this.state.kind !== 'idle') {
      return Promise.reject(new Error('Une action est déjà en cours'));
    }

    this.setState({kind: 'pendingUser', request});

    return new Promise<ActionResp>((resolve, reject) => {
      this.currentPromise = {resolve: resolve, reject: reject};
    });
  }

  async confirmAction(formData: ActionPerformerRequestParams): Promise<ActionResp> {
    if (!this.currentPromise) throw Error("No pending or waiting request");
    if (this.state.kind !== 'pendingUser') throw Error("No pending or waiting request");

    const {request} = this.state;
    this.setState({kind: 'running', request});

    try {
      const output:ActionResp = await this.execute(request, formData);
      this.setState({kind: 'done', request});
      this.currentPromise.resolve(output);
      return output
    } catch (e) {
      this.setState({kind: 'error', request, error: e});
      this.currentPromise.reject(e);
      throw e
    } finally {
      this.currentPromise = null;
    }

  }

  cancelAction(reason?: unknown) {
    if (!this.currentPromise) return;

    this.setState({kind: 'idle'});
    this.currentPromise.reject(reason ?? new Error('Action annulée'));
    this.currentPromise = null;
  }

  finishAction() {
    this.currentPromise = null;
    this.setState({kind: 'idle'});
  }

  private async execute(
    request: ActionRequest,
    formData: ActionPerformerRequestParams
  ): Promise<ActionResp> {

    const action = this.actionRegistry.findAction(request.actionGroupKey, request.actionKey)
    if (!action) {
      throw new Error(`Unknown action ${request.actionGroupKey}/${request.actionKey}`);
    }
    return await executeAction(request.actionGroupKey, request.actionKey, formData);
  }
}
