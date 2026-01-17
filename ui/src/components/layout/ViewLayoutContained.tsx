import type {PropsWithChildren} from "react";
import {type ReactNode} from "react";
import {makeStyles, tokens} from "@fluentui/react-components";

const useStyles = makeStyles({
  root: {
    display: "flex",
    flexDirection: "column",
    height: "100vh",
    maxHeight: "100vh",
    overflow: "hidden",
  },
  titleBar: {
    flex: 0,
    minHeight: "3em",
    backgroundColor: tokens.colorNeutralBackground3,
    borderBottom: `1px solid ${tokens.colorNeutralStroke1}`,
    textAlign: "center",
    fontWeight: tokens.fontWeightSemibold,
    height: "3em",
    verticalAlign: "middle",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  main: {
    flex: 1,
    overflowY: "auto",
    padding: "1em"
  }
})

export function ViewLayoutContained({title, children}: { title: ReactNode } & PropsWithChildren) {
  const styles = useStyles()
  return <div className={styles.root}>
    <div className={styles.titleBar}>
      <div>{title}</div>
    </div>
    <div className={styles.main}>{children}</div>
  </div>
}
