import {useEffect, useState} from "react";
import {Link} from "@tanstack/react-router";
import {ExternalUrl, Hashtags, Markdown, Origin} from "./ModelPage.tsx";
import {RelationshipDescription} from "./RelationshipDescription.tsx";
import type {EntityDto, ModelDto} from "../business/model.tsx";


export function EntityPage({modelId, entityDefId}: { modelId: string, entityDefId: string }) {

  const [model, setModel] = useState<ModelDto | undefined>(undefined);
  useEffect(() => {
    fetch("/ui/api/models/" + modelId, {headers: {"Accept": "application/json", "Content-Type": "application/json"}})
      .then(res => res.json())
      .then(json => setModel(json));
  }, [modelId, entityDefId])
  const entity = model?.entityDefs?.find(it => it.id === entityDefId)
  return <div>
    {model && entity && <EntityView entity={entity} model={model}/>}
  </div>
}

export function EntityView({entity, model}: { entity: EntityDto, model: ModelDto }) {
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
    <table>
      <tbody>{entity.attributes.map(a => <tr key={a.id}>
        <td style={{verticalAlign: "top"}}>{a.name ?? a.id}</td>
        <td>
          <div>
            <Markdown value={a.description}/>
          </div>
          <div>
            <code>{a.id}</code>
            {" "}
            <code>{a.type} {a.optional ? "?" : ""}</code>
            {a.identifierAttribute ? "🔑" : ""}
          </div>
        </td>
      </tr>)}</tbody>
    </table>
    <h2>Relationships involved</h2>
    <table>
      <tbody>{model.relationshipDefs
        .filter(it => it.roles.some(r => r.entityId === entity.id))
        .map(r => <tr key={r.id}>
          <td>{r.name ?? r.id}</td>
          <td><RelationshipDescription rel={r}/></td>
        </tr>)}</tbody>
    </table>

  </div>
}