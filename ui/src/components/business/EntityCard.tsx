import {Card, CardHeader, Text} from "@fluentui/react-components";
import type {EntityDto} from "../../business/model.tsx";
import {Tags} from "../core/Tag.tsx";

export function EntityCard({entity, onClick}:{entity: EntityDto, onClick:(id: string) => void}) {
  return <Card style={{width: "30%"}} onClick={() => onClick(entity.id)}>
    <CardHeader style={{height: "2em"}}
                header={<Text weight="semibold">{entity.name ?? entity.id}</Text>}></CardHeader>
    <div style={{minHeight: "4em", maxHeight: "4em", overflow: "hidden"}}>
      {entity.description && <div>{entity.description}</div>}
    </div>
    <div><Tags tags={entity.hashtags} /></div>
    <div><code>{entity.id}</code></div>
  </Card>
}