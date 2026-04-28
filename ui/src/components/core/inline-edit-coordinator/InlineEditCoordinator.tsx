/**
 * Identifies one mounted inline edit component.
 *
 * Created with useRef({}) in InlineEditSingleLineController and
 * InlineEditRichTextController. The coordinator compares these objects:
 * same object = same inline edit component
 * different object = another inline edit component
 */
export type InlineEditIdentifier = object;

/**
 * Functions passed by an open inline edit component to the coordinator.
 */
export type InlineEditCallbacks = {
  /**
   * Called when another inline edit wants to open.
   *
   * The current inline edit must try to cancel itself.
   * true = it closed
   * false = it stayed open
   */
  cancel: () => Promise<boolean>;
};

/**
 * Shared object used by inline edit components to keep only one editor open.
 */
export interface InlineEditCoordinator {
  /**
   * Called before an inline edit component opens.
   *
   * true = the caller may open
   * false = another inline edit component stayed open
   */
  requestEdit: (identifier: InlineEditIdentifier) => Promise<boolean>;
  /**
   * Stores the inline edit component that is now open.
   */
  registerActive: (
    identifier: InlineEditIdentifier,
    callbacks: InlineEditCallbacks,
  ) => void;
  /**
   * Clears the open inline edit component.
   *
   * It only clears when identifier is still the stored identifier. This avoids
   * closing another inline edit component by mistake.
   */
  clearActive: (identifier: InlineEditIdentifier) => void;
}
