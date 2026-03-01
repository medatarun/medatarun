import {Card, CardHeader, Text} from "@fluentui/react-components";
import type {EntityDto} from "@/business";
import {modelTagScope, Tags} from "@/components/core/Tag.tsx";
import {useDetailLevelContext} from "@/components/business/DetailLevelContext.tsx";
import {useModelContext} from "./ModelContext.tsx";

export function EntityCard({entity, onClick}:{entity: EntityDto, onClick:(id: string) => void}) {
  const model = useModelContext()
  const { isDetailLevelTech} = useDetailLevelContext()
  return <Card style={{width: "30%"}} onClick={() => onClick(entity.id)}>
    <CardHeader style={{height: "2em"}}
                header={<Text weight="semibold">{entity.name ?? entity.key ?? entity.id}</Text>}></CardHeader>
    <div style={{minHeight: "4em", maxHeight: "4em", overflow: "hidden"}}>
      {entity.description && <div>{entity.description}</div>}
    </div>
    <div><Tags tags={entity.tags} scope={modelTagScope(model.id)} /></div>
    { isDetailLevelTech &&
    <div><code>{entity.key}</code></div>
    }
  </Card>
}
