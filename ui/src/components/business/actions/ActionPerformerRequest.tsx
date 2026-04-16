/**
 * Action execution request captured at trigger time.
 *
 * This is like an event sent by a page to say "user wants to do that;
 * this is the context."
 *
 * Contract:
 * - params are action input values.
 * - displayedSubject describes where the action was launched from.
 *   Post-action navigation relies on it for delete behaviors.
 */
export type ActionPerformerRequest = {
  /**
   * Action group
   */
  actionGroupKey: string;
  /**
   * Action to launch
   */
  actionKey: string;
  /**
   * Known predefined parameters for the action, typically what is already
   * set before starting.
   */
  params: ActionPerformerRequestParams;
  /**
   * The object the action is about.
   * There is contextual information about where the user launches the action.
   *
   * Typically, which entity, which model, which tag, etc. It helps resolve
   * action parameters and resolve post-action behaviors.
   *
   * For example, for the "delete" actions on an object this helps to go back to
   * its parent object after deletion.
   */
  displayedSubject: ActionDisplayedSubject;
};

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

export const displaySubjectNone: ActionDisplayedSubjectNone = { kind: "none" };
