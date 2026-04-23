import type { ActionKey } from "@/business/action_registry/actionRegistry.dictionnary.ts";

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
   * Returns true if the param should be readonly
   */
  isReadonly: (paramKey: string) => boolean | undefined;

  /**
   * Returns true if the param should be visible
   */
  isVisible: (paramKey: string) => boolean | undefined;

  /**
   * Returns true if the param key is in the request default parameters
   */
  isPresent(paramKey: string): boolean;

  /**
   * Returns the default value for this parameter (or null if it is not present)
   */
  getDefaultValue: (paramKey: string, req: ActionPerformerRequest) => unknown;

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

const ACTION_CTX_NOWHERE: ActionCtx = {
  isVisible: () => true,
  isReadonly: () => true,
  getDefaultValue: () => null,
  isPresent: () => false,
  displayedSubject: displaySubjectNone,
};

/**
 * Use that when you have an action button that have no particular
 * context (screens without a particular object and no parameters to prefill),
 * for example, dashboards, lists, etc.
 */
export const createActionCtxVoid = () => {
  return ACTION_CTX_NOWHERE;
};

export interface ActionCtxMappingParam {
  actionGroupKey?: RegExp | string | undefined;
  actionKey?: RegExp | ActionKey | undefined;
  actionParamKey: string;
  defaultValue: () => unknown;
  readonly?: boolean;
  visible?: boolean;
}

export class ActionCtxMapping implements ActionCtx {
  modelMapping: Map<string, ActionCtxMappingParam>;
  displayedSubject: ActionDisplayedSubject;
  private mappings: ActionCtxMappingParam[];
  constructor(
    mappings: ActionCtxMappingParam[],
    displayedSubject: ActionDisplayedSubject,
  ) {
    this.displayedSubject = displayedSubject;
    this.mappings = mappings;
    this.modelMapping = new Map(mappings.map((it) => [it.actionParamKey, it]));
  }

  getDefaultValue = (paramKey: string, req: ActionPerformerRequest) => {
    const found = this.mappings.find((m) =>
      match(req.actionGroupKey, req.actionKey, paramKey, m),
    );
    if (found) {
      return found.defaultValue();
    }
    return undefined;
  };

  isPresent = (key: string): boolean => {
    return this.modelMapping.get(key) !== undefined;
  };
  isReadonly = (key: string) => {
    return this.modelMapping.get(key)?.readonly ?? false;
  };
  isVisible = (key: string) => {
    return this.modelMapping.get(key)?.visible ?? true;
  };
}

const matchValue = (
  reference: string,
  value: RegExp | string | undefined,
): boolean => {
  if (value == undefined) return true;
  if (typeof value == "string") return value === reference;
  if (value instanceof RegExp) return value.test(reference);
  return false;
};

const match = (
  actionGroupKey: string,
  actionKey: string,
  actionParamKey: string,
  mapping: ActionCtxMappingParam,
): boolean => {
  const matchGroup = matchValue(actionGroupKey, mapping.actionGroupKey);
  if (!matchGroup) return false;
  const matchAction = matchValue(actionKey, mapping.actionKey);
  if (!matchAction) return false;
  return actionParamKey === mapping.actionParamKey;
};
