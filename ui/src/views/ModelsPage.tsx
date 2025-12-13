import {useEffect, useState} from "react";
import {ModelCard} from "../components/business/ModelCard.tsx";
import type {ModelSummaryDto} from "../business/model.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ModelIcon} from "../components/business/Icons.tsx";
import {Breadcrumb, BreadcrumbButton} from "@fluentui/react-components";
import {ActionsBar} from "../components/business/ActionsBar.tsx";


export function ModelsPage({onClickModel}: { onClickModel: (modelId: string) => void }) {
  const [data, setData] = useState<ModelSummaryDto[]>([])
  useEffect(() => {
    fetch("/ui/api/models", {method: "GET", headers: new Headers({"Content-Type": "application/json"})})
      .then((res) => res.json())
      .then(json => setData(json))
  }, [])
  return <ViewLayoutContained title={<Breadcrumb><BreadcrumbButton icon={<ModelIcon/>}> Models</BreadcrumbButton></Breadcrumb>}>
    <ActionsBar location="models" />
    {data.length == 0 ? "No models found" : null}
    <div style={{display: "flex", columnGap: "1em", rowGap: "1em", flexWrap: "wrap", justifyContent: "space-around"}}>
      {data.map((model: ModelSummaryDto) => <ModelCard key={model.id} model={model} onClick={onClickModel}/>)}
    </div>

  </ViewLayoutContained>
}
