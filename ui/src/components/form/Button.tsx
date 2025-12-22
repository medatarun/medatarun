import { type ButtonProps, Button as FluentButton } from "@fluentui/react-components";
import { type ReactNode } from "react";

export function Button({
                         children,
                         variant,
                         disabled = false,
                         onClick,
                       }: {
  /**
   * Button contents (label of the button)
   */
  children: ReactNode;
  /**
   * Primary for main action buttons (OK, Next, etc.), "secondary" for Cancel, Back and secondary actions
   */
  variant?: "primary" | "secondary";
  /**
   * When it should be disabled, set it to true. If not specified or false, will not be disabled
   */
  disabled?: boolean;
  /**
   * When button is clicked
   */
  onClick: () => void;
}) {
  let mantineVariant: ButtonProps["appearance"] | undefined = undefined;
  if (variant === "secondary") mantineVariant = "secondary";
  else if (variant === "primary") mantineVariant = "primary";
  else mantineVariant = "primary";
  return (
    <FluentButton type="button" appearance={mantineVariant} disabled={disabled} onClick={onClick}>
      {children}
    </FluentButton>
  );
}
