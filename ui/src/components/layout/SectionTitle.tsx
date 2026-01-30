import type {PropsWithChildren, ReactNode} from "react";
import {type ActionUILocation, useActionRegistry} from "../../business";
import {Text, tokens} from "@fluentui/react-components";
import {ActionMenuButton} from "../business/TypesTable.tsx";
import type {ActionPerformerRequestParams} from "../business/ActionPerformer.tsx";

export function SectionTitle({icon, location, actionParams, children}: {
  icon: ReactNode,
  actionParams: ActionPerformerRequestParams,
  location: ActionUILocation
} & PropsWithChildren) {
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions(location)

  return <div style={{
    display: "flex",
    columnGap: tokens.spacingHorizontalS,
    alignItems: "center",
    justifyContent: "end",
    borderBottom: "1px solid #ccc",
    paddingBottom: tokens.spacingVerticalS,
    paddingRight: tokens.spacingVerticalS,
    marginTop: tokens.spacingVerticalXXXL,
    marginBottom: 0,
  }}>
    <div><Text weight="semibold" style={{display: "flex"}}>{icon}</Text></div>
    <div><Text weight="semibold">{children}</Text></div>
    <div><ActionMenuButton actionParams={actionParams} itemActions={actions}/></div>
  </div>
}