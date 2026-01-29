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
    //minHeight: "3em",
    backgroundColor: tokens.colorNeutralBackground2,
    borderBottom: `1px solid ${tokens.colorNeutralStroke1}`,
    textAlign: "left",
    fontWeight: tokens.fontWeightSemibold,
    verticalAlign: "middle",
    display: "flex",
    justifyContent: "start",
    alignItems: "center",
    paddingTop: tokens.spacingVerticalM

  },
  title: {
    width: "60rem",
    margin: "auto",
  },
  main: {
    flex: 1,
    overflowY: "auto"
  }
})

export function ViewLayoutContained({title, children}: { title?: ReactNode } & PropsWithChildren) {
  const styles = useStyles()
  return <div className={styles.root}>
    {title &&
    <div className={styles.titleBar}>
      <div className={styles.title}>{title}</div>
    </div>
    }
    <div className={styles.main}>{children}</div>
  </div>
}
