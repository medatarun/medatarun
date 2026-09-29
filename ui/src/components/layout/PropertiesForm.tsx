import type { PropsWithChildren } from "react";
import { makeStyles, tokens } from "@fluentui/react-components";

const useStyles = makeStyles({
  root: {
    display: "grid",
    gridTemplateColumns: "max-content auto",
    // columnGap: tokens.spacingVerticalM,
    columnGap: "0px",
    rowGap: "0px",
    // rowGap: tokens.spacingVerticalS,
    alignItems: "stretch",
    border: "2px solid " + tokens.colorNeutralStroke1,
  },
  label: {
    // backgroundColor: "lightblue",
    boxSizing: "border-box",
    height: "100%",
    minHeight: "2.3em",
    //height: "2.3em",
    display: "flex",
    alignItems: "center",
    color: tokens.colorNeutralForeground3,
    borderRight: "1px solid " + tokens.colorNeutralStroke1,
    borderBottom: "1px solid " + tokens.colorNeutralStroke1,
    paddingTop: tokens.spacingVerticalS,
    paddingBottom: tokens.spacingVerticalS,
    paddingLeft: tokens.spacingHorizontalS,
    paddingRight: tokens.spacingHorizontalS,
  },
  value: {
    // backgroundColor: "lightgreen",
    boxSizing: "border-box",
    minHeight: "2.3em",
    // height: "2.3em",
    display: "flex",
    alignItems: "center",
    minWidth: 0,
    paddingTop: tokens.spacingVerticalS,
    paddingBottom: tokens.spacingVerticalS,
    paddingLeft: tokens.spacingHorizontalS,
    paddingRight: tokens.spacingHorizontalS,
    backgroundColor: tokens.colorNeutralBackground1,
    borderBottom: "1px solid " + tokens.colorNeutralStroke1,
    "& > *": {
      flex: "1 1 auto",
      minWidth: 0,
    },
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
