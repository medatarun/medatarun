export type ActionPerformerRequestParams = Record<string, string>;
export type ActionRequest = {
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
  private state: ActionPerformerState = { kind: 'idle' };
  private listeners = new Set<Listener>();

  // pour relier la Promise initiée par performAction
  private currentPromise:
    | { resolve: () => void; reject: (e: unknown) => void }
    | null = null;

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

  performAction(location: string, params: ActionPerformerRequestParams): Promise<void> {
    if (this.state.kind !== 'idle') {
      return Promise.reject(new Error('Une action est déjà en cours'));
    }

    const request: ActionRequest = { location, params };

    this.setState({ kind: 'pendingUser', request });

    return new Promise<void>((resolve, reject) => {
      this.currentPromise = { resolve, reject };
    });
  }

  async confirmAction(formData: ActionPerformerRequestParams): Promise<void> {
    if (this.state.kind !== 'pendingUser' || !this.currentPromise) return;

    const { request } = this.state;
    this.setState({ kind: 'running', request });

    try {
      await this.execute(request, formData);
      this.setState({ kind: 'done', request });
      this.currentPromise.resolve();
    } catch (e) {
      this.setState({ kind: 'error', request, error: e });
      this.currentPromise.reject(e);
    } finally {
      this.currentPromise = null;
    }
  }

  cancelAction(reason?: unknown) {
    if (!this.currentPromise) return;

    this.setState({ kind: 'idle' });
    this.currentPromise.reject(reason ?? new Error('Action annulée'));
    this.currentPromise = null;
  }

  finishAction() {
    this.currentPromise = null;
    this.setState({ kind: 'idle' });
  }

  private async execute(
    request: ActionRequest,
    formData: ActionPerformerRequestParams
  ): Promise<void> {
    // Ici tu mets ta vraie logique métier : appel API, etc.
    // Je mets juste un wait pour l'exemple.
    console.log("action", request, formData)
    await new Promise(r => setTimeout(r, 500));
  }
}
