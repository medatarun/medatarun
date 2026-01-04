import {Caption1, Text} from "@fluentui/react-components";
import {ExternalUrl, Markdown, Origin} from "../../views/ModelPage.tsx";
import {Link} from "@tanstack/react-router";
import {Tags} from "../core/Tag.tsx";
import type {EntityDto} from "../../business";


export function EntityOverview({entity}: { entity: EntityDto}) {
  return <div>
    <div style={{
      float: "right",
      display: "float",
      border: "1px solid #ccc",
      padding: "1em",
      width: "20em",
      overflow: "hidden"
    }}>
      <div>
        <div style={{marginBottom: "1em"}}>
          <div><Caption1>Key</Caption1></div>
          <div><Text><code>{entity.id}</code></Text></div>
        </div>
        <div style={{marginBottom: "1em"}}>
          <div><Caption1>Model</Caption1></div>
          <div><Link to="/model/$modelId"
                     params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link></div>
        </div>
        <div style={{marginBottom: "1em"}}>
          <div><Caption1>External documentation</Caption1></div>
          <div><ExternalUrl url={entity.documentationHome}/></div>
        </div>
        <div style={{marginBottom: "1em"}}>
          <div><Caption1>Tags</Caption1></div>
          <div><Tags tags={entity.hashtags}/></div>
        </div>
        <div style={{marginBottom: "1em"}}>
          <div><Caption1>Origin</Caption1></div>
          <div><Origin value={entity.origin}/></div>
        </div>
      </div>
    </div>
    <div>
      <Markdown value={entity.description}/>
      <div>
        <Caption1 truncate style={{overflow: "hidden", width: "30em", display: "block",}}
                  wrap={false}>{entity.attributes.map(it => (it.id ?? it.name)).join(", ")}</Caption1>
      </div>
    </div>

  </div>

}