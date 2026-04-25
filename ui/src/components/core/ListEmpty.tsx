import type { PropsWithChildren } from "react";
import { makeStyles, tokens } from "@fluentui/react-components";
import { allStyles, type PropsWithStyle } from "@/components/core/utils.ts";

const useStyles = makeStyles({
  root: {
    borderRadius: tokens.borderRadiusMedium,
    border: `1px solid ${tokens.colorNeutralStroke1}`,
    padding: tokens.spacingVerticalM,
  },
});
export function ListEmpty({
  display,
  children,
  ...otherProps
}: { display: boolean } & PropsWithChildren & PropsWithStyle) {
  const styles = useStyles();
  const stylingProps = allStyles({ className: styles.root }, otherProps);
  if (!display) return;
  return <div {...stylingProps}>{children}</div>;
}
