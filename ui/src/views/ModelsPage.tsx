import {ModelCard} from "../components/business/ModelCard.tsx";
import {ActionUILocations, type ModelSummaryDto, useActionRegistry, useModelSummaries} from "../business";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ModelIcon} from "../components/business/Icons.tsx";
import {tokens} from "@fluentui/react-components";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {ActionMenuButton} from "../components/business/TypesTable.tsx";


export function ModelsPage({onClickModel}: { onClickModel: (modelId: string) => void }) {
  const {data = []} = useModelSummaries()
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions(ActionUILocations.models)

  return <ViewLayoutContained title={
    <div>
      <ViewTitle>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div><span><ModelIcon/></span> {" "} Models {" "}</div>
          <div>
            <ActionMenuButton label="Actions"
                              itemActions={actions}
                              actionParams={{}}/>
          </div>
        </div>
      </ViewTitle>
    </div>
  }>
    <div style={{
      paddingLeft: tokens.spacingHorizontalM,
      paddingRight: tokens.spacingHorizontalM,
    }}>
      {data.length == 0 ? "No models found" : null}
      <div style={{
        display: "flex",
        justifyContent: "center",
        columnGap: tokens.spacingVerticalM,
        rowGap: tokens.spacingVerticalM,
        flexWrap: "wrap",
        marginTop: tokens.spacingVerticalM
      }}>
        {data.map((model: ModelSummaryDto) => <ModelCard key={model.id} model={model} onClick={onClickModel}/>)}
      </div>
    </div>
  </ViewLayoutContained>
}
