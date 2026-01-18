import {Text, tokens} from "@fluentui/react-components";
import {ExternalUrl, Origin} from "../ModelPage.tsx";
import {Link} from "@tanstack/react-router";
import {Tags} from "../../components/core/Tag.tsx";
import type {EntityDto} from "../../business";


export function EntityOverview({entity}: { entity: EntityDto}) {
  return <div>
      <div style={{
        display: "grid",
        gridTemplateColumns: "min-content auto",
        columnGap: tokens.spacingVerticalM,
        rowGap: tokens.spacingVerticalM,
        alignItems: "baseline"
      }}>
          <div><Text>Entity&nbsp;code</Text></div>
          <div><Text><code>{entity.id}</code></Text></div>
          <div><Text>From&nbsp;model</Text></div>
          <div><Link to="/model/$modelId" params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link></div>
          <div><Text>Docs.&nbsp;home</Text></div>
          <div>{!entity.documentationHome ? <Text italic style={{color: tokens.colorNeutralStroke1}}>Not provided</Text> : <ExternalUrl url={entity.documentationHome}/> }</div>
          <div><Text>Tags</Text></div>
          <div>{ entity.hashtags.length == 0 ? <Text   italic style={{color:tokens.colorNeutralStroke1}}>Not tagged</Text> : <Tags tags={entity.hashtags}/> }</div>
          <div><Text>Origin</Text></div>
          <div><Origin value={entity.origin}/></div>
      </div>


  </div>

}