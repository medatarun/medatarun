import type {PropsWithChildren, ReactNode} from "react";
import {makeStyles, tokens} from "@fluentui/react-components";
import {ViewTitleEyebrow} from "./ViewTitleEyebrow.tsx";

const useStyles = makeStyles({
  root: {
    marginBottom: "0.6em",
  },
  mainTitle: {
    fontWeight: tokens.fontWeightSemibold,
    fontSize: tokens.fontSizeBase500,
    lineHeight: tokens.lineHeightBase500
  },
  mainTitleEllipsis: {
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap"
  }
})

export function ViewTitle({children, eyebrow}: { eyebrow?: ReactNode } & PropsWithChildren) {
  const styles = useStyles();
  return <div className={styles.root}>
    <ViewTitleEyebrow>{eyebrow}</ViewTitleEyebrow>
    <div className={styles.mainTitle}>
      <div className={styles.mainTitleEllipsis}>{children}</div>
    </div>
  </div>
}