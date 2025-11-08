import {useEffect, useState} from "react";

export function ModelPage({modelId}: { modelId: string }) {
  const [model, setModel] = useState<ModelDto | undefined>(undefined);
  useEffect(() => {
    fetch("ui/api/models/" + modelId, {headers: {"Accept": "application/json", "Content-Type": "application/json"}})
      .then(res => res.json())
      .then(json => setModel(json));
  }, [modelId])
  return <div>
    {model && <ModelView model={model}/>}
  </div>
}

interface ModelDto {
  id: string
  name: string | null
  version: string
  documentationHome: string | null
  hashtags: string[]
}

export function ModelView({model}: { model: ModelDto }) {
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
    </div>
  </div>
}

export function ExternalUrl({url}: { url: string | null }) {
  if (!url) return null
  return <a href={url} target="_blank">{url}</a>;
}

export function Hashtags({hashtags}: { hashtags: string[] }) {
  return <div>{hashtags.map((v, i) => <span className="tag" key={i}>{v}</span>)}</div>
}