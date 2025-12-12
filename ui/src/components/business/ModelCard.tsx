import {Card, CardHeader, Tag, TagGroup, Text} from "@fluentui/react-components";
import {EntityIcon, RelationshipIcon, TypeIcon} from "../business/Icons.tsx";
import type {ModelSummaryDto} from "../../business/model.tsx";

export function ModelCard({model, onClick}: { model: ModelSummaryDto, onClick: (id: string) => void }) {
  return <Card style={{maxWidth: "20em", width:"20em", minWidth:"20em"}} key={model.id} onClick={() => onClick(model.id)}>
    <CardHeader style={{height: "2em"}}
                header={<Text weight="semibold">{model.name ?? model.id}</Text>}></CardHeader>
    <div style={{minHeight: "4em", maxHeight: "4em", overflow: "hidden"}}>

      {model.description && <div>{model.description}</div>}
      {model.error && <div style={{color: "red"}}>{model.error}</div>}
    </div>
    <div><TagGroup size="extra-small">
      <Tag size="extra-small" appearance="outline" icon={<EntityIcon/>}>{model.countEntities}</Tag>
      <Tag size="extra-small" appearance="outline" icon={<RelationshipIcon/>}>{model.countRelationships}</Tag>
      <Tag size="extra-small" appearance="outline" icon={<TypeIcon/>}>{model.countTypes}</Tag>
    </TagGroup></div>
  </Card>
}
