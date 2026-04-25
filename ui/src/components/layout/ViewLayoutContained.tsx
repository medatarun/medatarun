import type { PropsWithChildren } from "react";
import { type ReactNode } from "react";
import { makeStyles, tokens } from "@fluentui/react-components";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";

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
    paddingTop: tokens.spacingVerticalM,
  },
  title: {
    maxWidth: "60rem",
    width: "60rem",
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
    margin: "auto",
  },
  main: {
    flex: 1,
    overflowY: "auto",
    backgroundColor: tokens.colorNeutralBackground1,
  },
  verticalSpacing: {
    display: "flex",
    flexDirection: "column",
    rowGap: tokens.spacingVerticalM,
    marginTop: tokens.spacingVerticalM,
  },
});

export function ViewLayoutContained({
  title,
  contained = false,
  scrollable = false,
  verticalSpacing = false,
  children,
}: {
  title?: ReactNode;
  contained?: boolean;
  scrollable?: boolean;
  verticalSpacing?: boolean;
} & PropsWithChildren) {
  const styles = useStyles();

  const verticalSpacingComponent = verticalSpacing ? (
    <div className={styles.verticalSpacing}>{children}</div>
  ) : (
    children
  );

  const containedComponent = contained ? (
    <ContainedHumanReadable>{verticalSpacingComponent}</ContainedHumanReadable>
  ) : (
    verticalSpacingComponent
  );
  const scollableComponent = scrollable ? (
    <ContainedMixedScrolling>
      <ContainedScrollable>{containedComponent}</ContainedScrollable>
    </ContainedMixedScrolling>
  ) : (
    containedComponent
  );

  return (
    <div className={styles.root}>
      {title && (
        <div className={styles.titleBar}>
          <div className={styles.title}>{title}</div>
        </div>
      )}
      <div className={styles.main}>{scollableComponent}</div>
    </div>
  );
}
