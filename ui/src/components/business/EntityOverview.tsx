import {Text, tokens} from "@fluentui/react-components";
import {ExternalUrl, Markdown, Origin} from "../../views/ModelPage.tsx";
import {Link} from "@tanstack/react-router";
import {Tags} from "../core/Tag.tsx";
import type {EntityDto} from "../../business";


export function EntityOverview({entity}: { entity: EntityDto}) {
  return <div>
      <div style={{
        display: "grid",
        gridTemplateColumns: "min-content auto",
        columnGap: tokens.spacingVerticalM,
        rowGap: tokens.spacingVerticalM,
        paddingTop: tokens.spacingVerticalM,
        alignItems: "baseline"
      }}>
          <div><Text>Key</Text></div>
          <div><Text><code>{entity.id}</code></Text></div>
          <div><Text>Model</Text></div>
          <div><Link to="/model/$modelId"
                     params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link></div>
          <div style={{whiteSpace: "nowrap"}}><Text>External&nbsp;documentation</Text></div>
          <div><ExternalUrl url={entity.documentationHome}/></div>
          <div><Text>Tags</Text></div>
          <div><Tags tags={entity.hashtags}/></div>
          <div><Text>Origin</Text></div>
          <div><Origin value={entity.origin}/></div>
      </div>
    <div>
      <Markdown value={entity.description}/>
    </div>

  </div>

}