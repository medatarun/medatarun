import type { PropsWithChildren } from "react";
import { makeStyles, mergeClasses, tokens } from "@fluentui/react-components";
import { allStyles, type PropsWithStyle } from "@/components/core/utils.ts";

export interface MessageBoxProps extends PropsWithChildren, PropsWithStyle {
  intent: "info" | "warning";
}
const useStyles = makeStyles({
  root: {
    backgroundColor: tokens.colorNeutralBackground2,
    border: "1px solid " + tokens.colorNeutralStroke2,
    padding: tokens.spacingVerticalM,
    borderRadius: tokens.borderRadiusMedium,
  },
  info: {
    backgroundColor: tokens.colorBrandBackground2,
    border: "1px solid " + tokens.colorBrandStroke2,
  },
  warning: {
    backgroundColor: tokens.colorStatusWarningBackground1,
    border: "1px solid " + tokens.colorStatusWarningBorder1,
  },
});
export function MessageBox({
  children,
  intent,
  ...otherProps
}: MessageBoxProps) {
  const styles = useStyles();
  const className = mergeClasses(
    styles.root,
    intent == "info"
      ? styles.info
      : intent == "warning"
        ? styles.warning
        : undefined,
  );
  return (
    <div {...allStyles(otherProps, { className: className })}>{children}</div>
  );
}
