import type { CSSProperties } from "react";
import { mergeClasses } from "@fluentui/react-components";

/**
 * Use that on components to say "you can override className and style"
 */
export type PropsWithStyle = {
  style?: CSSProperties | undefined;
  className?: string | undefined;
};

/**
 * This will merge your component base style with provided customised style and classname
 *
 * For usage in components that provide PropsWithStyle
 *
 * ```
 * <div {...allStyles({className: componentClassName, styles: componentInlineStyle}, {...customProps})}>
 * ```
 */
export function allStyles(
  base?: PropsWithStyle,
  custom?: PropsWithStyle & Record<string, unknown>,
): PropsWithStyle {
  const mergedClassName = mergeClassNames(base?.className, custom?.className);
  const mergedStyle = mergeStyles(base?.style, custom?.style);
  return { className: mergedClassName, style: mergedStyle };
}

function mergeClassNames(
  base: string | undefined,
  custom: string | undefined,
): string | undefined {
  if (!custom) return base;
  if (!base) return custom;
  return mergeClasses(base, custom);
}
function mergeStyles(
  base: CSSProperties | undefined,
  custom: CSSProperties | undefined,
): CSSProperties | undefined {
  if (!custom) return base;
  if (!base) return custom;
  return { ...base, ...custom };
}
