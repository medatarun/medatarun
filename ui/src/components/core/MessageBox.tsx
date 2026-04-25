import type { CSSProperties, PropsWithChildren } from "react";
import { makeStyles, mergeClasses, tokens } from "@fluentui/react-components";

export interface MessageBoxProps extends PropsWithChildren {
  intent: "info" | "warning";
  styles?: CSSProperties;
  className?: string;
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
  className,
  styles,
}: MessageBoxProps) {
  const style = useStyles();
  const classNames = mergeClasses(
    style.root,
    intent == "info"
      ? style.info
      : intent == "warning"
        ? style.warning
        : undefined,
    className,
  );
  return (
    <div style={styles} className={classNames}>
      {children}
    </div>
  );
}
