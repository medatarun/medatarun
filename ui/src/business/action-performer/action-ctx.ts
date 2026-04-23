import type { ActionRequest } from "./action-request.ts";
import {
  type ActionDisplayedSubject,
  displaySubjectNone,
} from "./action-ctx-displayed-subject.ts";

/**
 * Action context gives the action performer information about where an action
 * is launched from.
 *
 * For example, a page will give context on its displayed main item. A table
 * row or list item will give context on the list item displayed.
 *
 * When the action is processed, we'll try to see what parameters the
 * action requires. We will try to get most parameters from the context.
 *
 * If all required parameters to launch the action are filled and readonly,
 * the action will be processed immediately. Otherwise, a form may appear to
 * ask the user for additional inputs. This will also help determine which fields
 * appear on the screen, with which prefilled values, based on what we are currently
 * displaying next to the action button.
 */
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

  /* TODO: find a way to not pass the full ActionPerformerRequest */
  /**
   * Returns the default value for this parameter (or null if it is not present)
   */
  getDefaultValue: (paramKey: string, req: ActionRequest) => unknown;

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
