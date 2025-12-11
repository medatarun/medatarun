import {useEffect, useState} from "react";
import {Card, CardHeader, Tag, TagGroup, Text} from "@fluentui/react-components";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {EntityIcon, RelationshipIcon, TypeIcon} from "../components/business/Icons.tsx";

interface ModelSummaryDto {
  id: string,
  name: string | null,
  description: string | null,
  error: string | null,
  countTypes: number,
  countEntities: number,
  countRelationships: number
}

export function ModelsPage({onClickModel}: { onClickModel: (modelId: string) => void }) {
  const [data, setData] = useState<ModelSummaryDto[]>([])
  useEffect(() => {
    fetch("/ui/api/models", {method: "GET", headers: new Headers({"Content-Type": "application/json"})})
      .then((res) => res.json())
      .then(json => setData(json))
  }, [])
  return <div>
    <ViewTitle>Models</ViewTitle>
    {data.length == 0 ? "No models found" : null}
    <div style={{display:"flex", columnGap:"1em", rowGap:"1em", flexWrap:"wrap"}}>
        {data.map((model: ModelSummaryDto) => <Card style={{width:"30%"}} key={model.id} onClick={() => onClickModel(model.id)}>
          <CardHeader style={{height:"2em"}} header={<Text weight="semibold" >{model.name ?? model.id}</Text>}></CardHeader>
          <div style={{minHeight:"4em", maxHeight:"4em", overflow:"hidden"}}>

            {model.description && <div>{model.description}</div>}
            {model.error && <div style={{color: "red"}}>{model.error}</div>}
          </div>
          <div><TagGroup size="extra-small">
            <Tag size="extra-small" appearance="outline" icon={<EntityIcon/>}>{model.countEntities}</Tag>
            <Tag size="extra-small" appearance="outline" icon={<RelationshipIcon/>}>{model.countRelationships}</Tag>
            <Tag size="extra-small" appearance="outline" icon={<TypeIcon/>}>{model.countTypes}</Tag>
          </TagGroup></div>
        </Card>)}
    </div>

  </div>
}
