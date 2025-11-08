import {useEffect, useState} from "react";
import {Link} from "@tanstack/react-router";
import {ExternalUrl, Hashtags, Markdown, Origin} from "./ModelPage.tsx";

export interface EntityDto {
  id: string
  name: string | null
  description: string | null
  origin: EntityDefOriginDto
  documentationHome: string | null
  hashtags: string[]
  attributes: EntityAttributeDto[]
  model: {
    id: string
    name: string | null
  }
}

interface EntityAttributeDto {
  id: string
  type: string
  optional: boolean
  identifierAttribute: boolean
  name: string | null
  description: string | null
}

interface EntityDefOriginDto {
  type: "manual" | "uri",
  uri: string | null

}

export function EntityPage({modelId, entityDefId}: { modelId: string, entityDefId: string }) {
  const [entity, setEntity] = useState<EntityDto | undefined>(undefined)
  useEffect(() => {
    fetch("/ui/api/models/" + modelId + "/entitydefs/" + entityDefId)
      .then((res) => res.json())
      .then(data => setEntity(data))
  }, [modelId, entityDefId])
  return <div>
    {entity && <EntityView entity={entity}/>}
  </div>
}

export function EntityView({entity}: { entity: EntityDto }) {
  return <div>
    <h1>Entity {entity.name ?? entity.id}</h1>
    <div style={{display: "grid", gridTemplateColumns: "auto auto", columnGap: "1em", marginBottom: "1em"}}>
      <div>Identifier</div>
      <div>{entity.id}</div>
      <div>Model</div>
      <div>
        <Link to="/model/$modelId" params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link>
      </div>
      <div>Documentation</div>
      <div><ExternalUrl url={entity.documentationHome}/></div>
      <div>Hashtags</div>
      <div><Hashtags hashtags={entity.hashtags}/></div>
      <div>Origin</div>
      <div><Origin value={entity.origin}/></div>
    </div>
    <div>
      <Markdown value={entity.description}/>
    </div>
    <h2>Attributes</h2>
    <table><tbody>{entity.attributes.map(a => <tr key={a.id}>
      <td style={{verticalAlign: "top"}}>{a.name ?? a.id}</td>
      <td>
        <div>
          <Markdown value={a.description}/>
        </div>
        <div>
          <code>{a.id}</code>
          { " " }
          <code>{a.type} {a.optional ? "?" : ""}</code>
          {a.identifierAttribute ? "🔑" : ""}
        </div>
      </td>
    </tr>)}</tbody></table>
  </div>
}