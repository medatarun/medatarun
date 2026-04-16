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
   * Contextual information for the action (prefilled parameters, where we
   * are on screens, etc.)
   */
  ctx: ActionCtx;
};

export type ActionPerformerRequestParam = {
  visible: boolean;
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

export type ActionCtx = {
  /**
   * Known predefined parameters for the action, typically what is already
   * set before starting.
   */
  actionParams: ActionPerformerRequestParams;
  /**
   * The object the action is about.
   * There is contextual information about where the user launches the action.
   *
   * Typically, which entity, which model, which tag, etc. It helps resolve
   * action parameters and resolve post-action behaviors.
   *
   * For example, for the "delete" actions on an object this helps to go back to
   * its parent object after deletion.
   *
   * Keep it equal to the page displayed subject (not the object on which
   * the action is executed). For example, if the action is on an attribute
   * in an entity page, this will be the Entity not the attribute.
   */
  displayedSubject: ActionDisplayedSubject;
};

/**
 * Factory to create action ctx. Never try to create an ActionCtx without
 * using one of the provided methods
 * @param props
 */
export const createActionCtx = (props: {
  actionParams: ActionPerformerRequestParams;
  displayedSubject: ActionDisplayedSubject;
}): ActionCtx => ({
  actionParams: props.actionParams,
  displayedSubject: props.displayedSubject,
});

const ACTION_CTX_NOWHERE = {
  actionParams: {},
  displayedSubject: displaySubjectNone,
};

/**
 * Use that when you have an action button that have no particular
 * context (screens without a particular object and no parameters to prefill),
 * for example, dashboards, lists, etc.
 */
export const createActionCtxVoid = () => {
  return createActionCtx(ACTION_CTX_NOWHERE);
};
