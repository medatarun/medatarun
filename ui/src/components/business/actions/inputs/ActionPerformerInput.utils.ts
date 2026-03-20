import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";

/**
 * Takes the undefined value from {@link ActionPerformerInputProps} and convert
 * it to a safe string or empty
 */
export function normalizeValueStringOrEmpty(value: unknown) {
  if (value === null) return "";
  if (value === undefined) return "";
  return "" + value;
}
/**
 * Takes the undefined value from {@link ActionPerformerInputProps} and convert
 * it to a safe string or empty
 */
export function normalizeValueBooleanOrNull(value: unknown) {
  if (value === null) return null;
  if (value === undefined) return null;
  return value === true;
}
/**
 * Takes the undefined value from {@link ActionPerformerInputProps} and convert
 * it to a string or null
 */
export function normalizeValueStringOrNull(value: unknown) {
  if (value === null) return null;
  if (value === undefined) return null;
  return "" + value;
}

/**
 * Given a ref, returns a row id. For example "id:xxx" -> "xxx"
 *
 * If value is null returns null
 */
export function refIdToRawId(value: string | null): string | null {
  if (value == null) return null;
  return value.replace(/^id:/, "");
}

/**
 * Given a raw id, returns a ref by id
 *
 * For example, "xxx" -> "id:xxx"
 */
export function rawIdToRefId(value: string | null): string | null {
  if (value == null) return null;
  return "id:" + value;
}

/**
 * Utility wrapper that takes {@link ActionPerformerInputProps} and wraps it
 * into a new set of props where refs are converted to ids.
 *
 * ref by id (from action performer) <-> id (for the component)
 *
 * Used in components that allow users to select ids, but the ActionPerformer
 * requires refs (id:xxx) not just an id
 *
 * - the "ref by id" value is converted to "id" value, so the
 *   component has an id
 * - the onValueChange is adapted to convert value as "id" to a "ref by id"
 *
 */
export function adaptPropsRefIdToRawId(
  props: ActionPerformerInputProps,
): ActionPerformerInputProps<string> {
  const valueSafe = normalizeValueStringOrNull(props.value);
  const id = refIdToRawId(valueSafe);
  return {
    ...props,
    value: id,
    onValueChange: (value: string) =>
      props.onValueChange(rawIdToRefId(normalizeValueStringOrNull(value))),
  };
}

/**
 * Utility wrapper that exposes nullable string values as empty strings for
 * select-like components and converts the empty selection back to null.
 */
export function adaptPropsValueNullableToValueEmpty(
  props: ActionPerformerInputProps,
): ActionPerformerInputProps<string> {
  return {
    ...props,
    value: normalizeValueStringOrEmpty(props.value),
    onValueChange: (value: string) => {
      const valueSafe = normalizeValueStringOrEmpty(value);
      props.onValueChange(valueSafe === "" ? null : valueSafe);
    },
  };
}
