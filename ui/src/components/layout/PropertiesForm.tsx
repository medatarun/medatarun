import type {PropsWithChildren} from "react";
import {makeStyles, tokens} from "@fluentui/react-components";

const useStyles = makeStyles({
  root: {
    display: "grid",
    gridTemplateColumns: "min-content auto",
    columnGap: tokens.spacingVerticalM,
    rowGap: tokens.spacingVerticalS,
    "& div": {
      boxSizing: "border-box",
      minHeight: "2.3em",
      height: "2.3em",
      lineHeight: "2.1em",
      "& label": {
        minHeight: "2.3em",
        height: "2.3em",
        lineHeight: "2.1em",
      }
    }
  }
})

export function PropertiesForm({children}: PropsWithChildren) {
  const styles = useStyles()
  return <div className={styles.root}>{children}
  </div>
}