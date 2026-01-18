import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {tokens} from "@fluentui/react-components";
import {ActionMenuButton} from "../components/business/TypesTable.tsx";
import {ActionUILocations, useActionRegistry} from "../business";
import {MissingInformation} from "../components/core/MissingInformation.tsx";
import {ContainedHumanReadable} from "../components/layout/Contained.tsx";

export function DashboardPage() {
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions(ActionUILocations.global)
  return <ViewLayoutContained title={
    <div>
      <ViewTitle eyebrow="Dashboard">
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div>Dashboard</div>
          <div><ActionMenuButton
            label="Actions"
            itemActions={actions}
            actionParams={{}}/></div>
        </div>
      </ViewTitle>
    </div>
  }>
    <ContainedHumanReadable>
      <div>
        <MissingInformation>Currently creating the Dashboard</MissingInformation>
      </div>
    </ContainedHumanReadable>
  </ViewLayoutContained>
}