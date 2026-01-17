import type {PropsWithChildren, ReactNode} from "react";
import {useActionRegistry} from "../../business";
import {Text, tokens} from "@fluentui/react-components";
import {ActionMenuButton} from "../business/TypesTable.tsx";

export function SectionTitle({icon, location, actionParams, children}: {
  icon: ReactNode,
  actionParams: Record<string, string>,
  location: string
} & PropsWithChildren) {
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions(location)

  return <div style={{
    display: "flex",
    columnGap: tokens.spacingHorizontalS,
    alignItems: "center",
    justifyContent: "end",
    borderBottom: "1px solid #ccc",
    paddingBottom: tokens.spacingVerticalS
  }}>
    <div><Text weight="semibold" style={{display: "flex"}}>{icon}</Text></div>
    <div><Text weight="semibold">{children}</Text></div>
    <div><ActionMenuButton actionParams={actionParams} itemActions={actions}/></div>
  </div>
}