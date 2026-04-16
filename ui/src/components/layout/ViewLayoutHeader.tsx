import type { ReactNode } from "react";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { tokens } from "@fluentui/react-components";
import { ActionDescriptor } from "@/business/action_registry";
import type { ActionCtx } from "@/components/business/actions";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

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
  return (
    <div>
      {props.breadcrumb && <div>{props.breadcrumb}</div>}
      <ViewTitle eyebrow={props.eyebrow}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            paddingRight: tokens.spacingHorizontalL,
            columnGap: tokens.spacingHorizontalS,
            alignItems: "center",
            justifyItems: "stretch",
          }}
        >
          {props.titleIcon && (
            <div style={{ display: "flex" }}>{props.titleIcon}</div>
          )}
          <div style={{ width: "100%" }}>{props.title}</div>
          {props.actions && (
            <div>
              <ActionMenuButton
                label={props.actions.label}
                itemActions={props.actions.itemActions}
                actionCtx={props.actions.actionCtx}
              />
            </div>
          )}
        </div>
      </ViewTitle>
    </div>
  );
}
