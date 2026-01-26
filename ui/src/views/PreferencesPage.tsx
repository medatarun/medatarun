import {SwitchButton} from "@seij/common-ui";
import {useDetailLevelContext} from "../components/business/DetailLevelContext.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {tokens} from "@fluentui/react-components";
import {ActionMenuButton} from "../components/business/TypesTable.tsx";
import {ContainedHumanReadable} from "../components/layout/Contained.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ActionUILocations, useActionRegistry} from "../business";
import {createActionTemplateGeneral} from "../components/business/actionTemplates.ts";

export function PreferencesPage() {
  const {isDetailLevelTech, toggle} = useDetailLevelContext()
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions(ActionUILocations.preferences)
  return <ViewLayoutContained
    title={
      <div>
        <ViewTitle eyebrow="">
          <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
            <div>Preferences</div>
            <div><ActionMenuButton
              label="Actions"
              itemActions={actions}
              actionParams={createActionTemplateGeneral()}/></div>
          </div>
        </ViewTitle>
      </div>
    }>
    <ContainedHumanReadable>
      <div>
        <div>
          <p>You are currently in {isDetailLevelTech ? "tech" : "business"} mode.</p>
          <p>Switching modes allow you to show or hide technical details in the models.</p>
        </div>
        <SwitchButton
          value={isDetailLevelTech}
          onValueChange={toggle}
          labelTrue={"Switch to business mode"}
          labelFalse={"Switch to technical mode"}/>
      </div>
    </ContainedHumanReadable>
  </ViewLayoutContained>
}


