import {useEffect, useState} from "react";

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
  },[])
  return <div>
    <h1>Models</h1>
    <table>
      <tbody>
      {data.map((model: ModelSummaryDto) => <tr key={model.id}>
        <td><a href="#" onClick={() => onClickModel(model.id)}>{model.name ?? model.id}</a></td>
        <td>
          <div style={{display: 'flex', justifyContent: 'space-between'}}>
            <div><code>{model.id}</code></div>
            <div>{model.countEntities}×E {model.countRelationships}×R {model.countTypes}×T</div>
          </div>
          {model.description && <div>{model.description}</div>}
          {model.error && <div style={{color: "red"}}>{model.error}</div>}
        </td>
      </tr>)}
      </tbody>
    </table>
  </div>
}

