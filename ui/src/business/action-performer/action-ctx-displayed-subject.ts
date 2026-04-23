/**
 * Subject currently displayed by the UI where the action is triggered.
 *
 * This is mainly used when we launch an action where a particular subject
 * is displayed, and we need to move or change screens depending on the action.
 *
 * For example, if the page represents a "model" or a "role" and we need to
 * delete it, then the page subject is this "model" or "role".
 *
 * Example:
 *
 * When a delete action on this subject will be triggered, the subject of the
 * action should match the subject of the page. If it matches, we are able to say
 * "move out of here". On the opposite, if we display a "model" page and the
 * action is on an "entity" displayed in the page, this doesn't match, so
 * we know we don't need to move out.
 *
 * When "none" then it means there is no particular subject displayed on screen.
 *
 * Contract:
 * - This is page-level context, not per-item displayed on the page context.
 * - refs keys must follow action naming conventions (modelRef, entityRef, ...).
 * - refs values should use the same normalized format as action params ("id:..." or "key:...").
 */
export type ActionDisplayedSubject =
  | ActionDisplayedSubjectNone
  | ActionDisplayedSubjectResource;

/**
 * Indicates no particular subject is displayed
 */
export type ActionDisplayedSubjectNone = { kind: "none" };

/**
 * Indicates a real subject, with its coordinates
 */
export type ActionDisplayedSubjectResource = {
  kind: "resource";
  /**
   * Type of the subject ("model", "entity", "role", etc.)
   */
  type: string;
  /**
   * List of references that identifies the subject currently displayed
   * modelRef to "id:xxx", entityRef to "id:xxx"
   **/
  refs: Record<string, string>;
};

export const displaySubjectNone: ActionDisplayedSubjectNone = { kind: "none" };
