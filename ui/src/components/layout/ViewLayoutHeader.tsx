import type { ReactNode } from "react";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { makeStyles, tokens } from "@fluentui/react-components";
import { ActionDescriptor } from "@/business/action_registry";
import type { ActionCtx } from "@/components/business/actions";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

const useStyles = makeStyles({
  breadcrumb: {
    marginBottom: tokens.spacingVerticalM,
  },
  title: {
    display: "flex",
    justifyContent: "space-between",
    columnGap: tokens.spacingHorizontalS,
    alignItems: "center",
    justifyItems: "stretch",
  },
  titleIcon: { display: "flex" },
  titleTitle: { width: "100%" },
});

export interface ViewLayoutHeaderProps {
  breadcrumb?: ReactNode;
  eyebrow?: ReactNode;
  title: ReactNode;
  titleIcon: ReactNode;
  actions?: {
    label: string;
    itemActions: ActionDescriptor[];
    actionCtx: ActionCtx;
  };
}
export function ViewLayoutHeader(props: ViewLayoutHeaderProps) {
  const styles = useStyles();
  return (
    <div>
      {props.breadcrumb && (
        <div className={styles.breadcrumb}>{props.breadcrumb}</div>
      )}
      <ViewTitle eyebrow={props.eyebrow}>
        <div className={styles.title}>
          {props.titleIcon && (
            <div className={styles.titleIcon}>{props.titleIcon}</div>
          )}
          <div className={styles.titleTitle}>{props.title}</div>
          {props.actions && (
            <ActionMenuButton
              label={props.actions.label}
              itemActions={props.actions.itemActions}
              actionCtx={props.actions.actionCtx}
            />
          )}
        </div>
      </ViewTitle>
    </div>
  );
}
