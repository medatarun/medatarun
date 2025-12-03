import {useEffect, useState} from "react";
import ReactMarkdown from "react-markdown";
import {Link} from "@tanstack/react-router";
import {RelationshipDescription} from "./RelationshipDescription.tsx";
import {type ElementOrigin, Model, type ModelDto} from "../business/model.tsx";
import {ModelContext, useModelContext} from "./ModelContext.tsx";

export function ModelPage({modelId}: { modelId: string }) {
  const [model, setModel] = useState<ModelDto | undefined>(undefined);
  useEffect(() => {
    fetch("/ui/api/models/" + modelId, {headers: {"Accept": "application/json", "Content-Type": "application/json"}})
      .then(res => res.json())
      .then(json => setModel(json));
  }, [modelId])
  return <div>
    {model && <ModelContext value={new Model(model)}><ModelView /></ModelContext>}
  </div>
}


export function ModelView() {
  const model = useModelContext().dto
  return <div>
    <h1>Model {model.name ?? model.id}</h1>
    <div style={{display: "grid", gridTemplateColumns: "min-content auto", columnGap: "1em"}}>
      <div>Identifier</div>
      <div><code>{model.id}</code></div>
      <div>Version</div>
      <div><code>{model.version}</code></div>
      <div>Documentation</div>
      <div><ExternalUrl url={model.documentationHome}/></div>
      <div>Hashtags</div>
      <div><Hashtags hashtags={model.hashtags}/></div>
      <div>Origin</div>
      <div><Origin value={model.origin}/></div>
    </div>
    {model.description && <div><Markdown value={model.description}/></div>}
    <h2>Entities</h2>
    <table>
      <tbody>
      {
        model.entityDefs.map(entityDef => <tr key={entityDef.id}>
          <td><Link to="/model/$modelId/entityDef/$entityDefId"
                    params={{modelId: model.id, entityDefId: entityDef.id}}>{entityDef.name ?? entityDef.id}</Link></td>
          <td>{entityDef.description}</td>
        </tr>)
      }
      </tbody>
    </table>
    <h2>Relationships</h2>
    <table>
      <tbody>
      {model.relationshipDefs.map(r => <tr key={r.id}>
        <td>{r.name ?? r.id}</td>
        <td><RelationshipDescription rel={r} /></td>
      </tr>)}
      </tbody>
    </table>
    <h2>Types</h2>
    <table>
      <tbody>
      {
        model.types.map(t => <tr key={t.id}>
          <td>{t.name ?? t.id}</td>
          <td>{t.description}</td>
        </tr>)
      }
      </tbody>
    </table>
  </div>
}

export function Origin({value}: { value: ElementOrigin }) {
  if (value.type == "manual") return "Medatarun (manual)"
  return <ExternalUrl url={value.uri}/>
}

export function ExternalUrl({url}: { url: string | null }) {
  if (!url) return null
  return <a href={url} target="_blank">{url}</a>;
}

export function Hashtags({hashtags}: { hashtags: string[] }) {
  return <div>{hashtags.map((v, i) => <span className="tag" key={i}>{v}</span>)}</div>
}

export function Markdown({value}: { value: string | null }) {
  if (value == null) return null
  return <ReactMarkdown>{value}</ReactMarkdown>
}
