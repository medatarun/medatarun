import {useEffect, useState} from "react";

import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {ModelCard} from "../components/business/ModelCard.tsx";
import type {ModelSummaryDto} from "../business/model.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";


export function ModelsPage({onClickModel}: { onClickModel: (modelId: string) => void }) {
  const [data, setData] = useState<ModelSummaryDto[]>([])
  useEffect(() => {
    fetch("/ui/api/models", {method: "GET", headers: new Headers({"Content-Type": "application/json"})})
      .then((res) => res.json())
      .then(json => setData(json))
  }, [])
  return <ViewLayoutContained>
    <ViewTitle>Models</ViewTitle>
    {data.length == 0 ? "No models found" : null}
    <div style={{display: "flex", columnGap: "1em", rowGap: "1em", flexWrap: "wrap"}}>
      {data.map((model: ModelSummaryDto) => <ModelCard key={model.id} model={model} onClick={onClickModel}/>)}
    </div>

  </ViewLayoutContained>
}
