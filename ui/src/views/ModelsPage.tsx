import {ModelCard} from "../components/business/ModelCard.tsx";
import {type ModelSummaryDto, useModelSummaries} from "../business";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ModelIcon} from "../components/business/Icons.tsx";
import {Breadcrumb, BreadcrumbButton} from "@fluentui/react-components";
import {ActionsBar} from "../components/business/ActionsBar.tsx";


export function ModelsPage({onClickModel}: { onClickModel: (modelId: string) => void }) {
  const {data = []} = useModelSummaries()
  return <ViewLayoutContained title={<Breadcrumb><BreadcrumbButton icon={<ModelIcon/>}> Models</BreadcrumbButton></Breadcrumb>}>
    <ActionsBar location="models" />
    {data.length == 0 ? "No models found" : null}
    <div style={{display: "flex", columnGap: "1em", rowGap: "1em", flexWrap: "wrap"}}>
      {data.map((model: ModelSummaryDto) => <ModelCard key={model.id} model={model} onClick={onClickModel}/>)}
    </div>

  </ViewLayoutContained>
}
