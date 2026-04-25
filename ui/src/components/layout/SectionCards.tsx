import type { PropsWithChildren } from "react";

/**
 * A section of a page containing cards.
 *
 * This should not define cards layouts as it may change a lot from
 * one need to another.
 *
 * This is just for the "border" and "background" commons
 */
export function SectionCards({ children }: PropsWithChildren) {
  return <div>{children}</div>;
}
