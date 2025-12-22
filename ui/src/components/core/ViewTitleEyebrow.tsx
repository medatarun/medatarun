import type {PropsWithChildren} from "react";
import {makeStyles, tokens} from "@fluentui/react-components";

const useStyles = makeStyles({
  root: {
    fontSize: "0.8em",
    textTransform: "uppercase",
    color: tokens.colorNeutralForeground3
  }
})

export function ViewTitleEyebrow({children}: PropsWithChildren) {
  const styles = useStyles();
  return <div className={styles.root}>{children}</div>
}