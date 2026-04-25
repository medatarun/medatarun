import type { PropsWithChildren } from "react";
import { makeStyles, Text, tokens } from "@fluentui/react-components";

const useStyles = makeStyles({
  root: {
    display: "grid",
    gridTemplateColumns: "max-content auto",
    columnGap: tokens.spacingVerticalM,
    rowGap: tokens.spacingVerticalS,
    alignItems: "baseline",
  },
  label: {
    // backgroundColor: "lightblue",
    boxSizing: "border-box",
    minHeight: "2.3em",
    height: "2.3em",
    lineHeight: "2.1em",
    color: tokens.colorNeutralForeground3,
  },
  value: {
    // backgroundColor: "lightgreen",
    boxSizing: "border-box",
    minHeight: "2.3em",
    height: "2.3em",
    lineHeight: "2.1em",
  },
});

export function PropertiesForm({ children }: PropsWithChildren) {
  const styles = useStyles();
  return <div className={styles.root}>{children}</div>;
}
export function PropertyLabel({ children }: PropsWithChildren) {
  const styles = useStyles();
  return <div className={styles.label}>{children}</div>;
}
export function PropertyValue({ children }: PropsWithChildren) {
  const styles = useStyles();
  return <div className={styles.value}>{children}</div>;
}
