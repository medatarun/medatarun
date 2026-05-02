/**
 * Describes a form field to display in action performer form.
 */
export type ActionFormFieldDescription = {
  /**
   * Unique identifier of the form field in the context of the form
   */
  key: string;
  /**
   * Display title
   */
  title: string;
  /**
   * Description of the field to display
   */
  description: string | null;
  /**
   * Indicates if the field is required or not
   */
  optional: boolean;
  /**
   * Data type to handle. Must be one of the types declared in the type system
   * using the Multiplatform name of the type
   */
  type: string;
  /**
   * Display order of the field
   */
  order: number;
  /**
   * Indicates if the field should be readonly. Usually depends on the context
   * from where the action is launched. For example, if you are on a tag page and
   * try to launch an update-something action, the tag id will be readonly.
   */
  readonly: boolean;
  /**
   * Indicates if the field should be visibile. Usually depends on the context
   * from where the action is launched. For example, if you are on a tag page and
   * try to launch an update-something action, the tag id will be invisible.
   */
  visible: boolean;
};
