import {ModelCard} from "../components/business/ModelCard.tsx";
import {type ModelSummaryDto, useActionRegistry, useModelSummaries} from "../business";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ModelIcon} from "../components/business/Icons.tsx";
import {Breadcrumb, BreadcrumbButton, Divider, tokens} from "@fluentui/react-components";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {ActionMenuButton} from "../components/business/TypesTable.tsx";


export function ModelsPage({onClickModel}: { onClickModel: (modelId: string) => void }) {
  const {data = []} = useModelSummaries()
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions("models")

  return <ViewLayoutContained title={<Breadcrumb><BreadcrumbButton icon={<ModelIcon/>}> Models</BreadcrumbButton></Breadcrumb>}>
    <div style={{
      paddingLeft: tokens.spacingHorizontalM,
      paddingRight: tokens.spacingHorizontalM,
    }}>
      <div style={{margin: "auto", width: "80rem"}}>
        <div style={{
          marginTop: tokens.spacingVerticalM,
        }}>
          <ViewTitle>
            <span><ModelIcon/></span> { " " } Models { " " }
            <ActionMenuButton
              itemActions={actions}
              actionParams={{}}/>
          </ViewTitle>
          <Divider/>
        </div>
      </div>

    {data.length == 0 ? "No models found" : null}
    <div style={{display: "flex", justifyContent:"center", columnGap: tokens.spacingVerticalM, rowGap: tokens.spacingVerticalM, flexWrap: "wrap", marginTop:tokens.spacingVerticalM}}>
      {data.map((model: ModelSummaryDto) => <ModelCard key={model.id} model={model} onClick={onClickModel}/>)}
    </div>
    </div>
  </ViewLayoutContained>
}
