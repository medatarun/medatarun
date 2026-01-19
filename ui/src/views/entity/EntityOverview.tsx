import {Text} from "@fluentui/react-components";
import {ExternalUrl, Origin} from "../ModelPage.tsx";
import {Link} from "@tanstack/react-router";
import {Tags} from "../../components/core/Tag.tsx";
import type {EntityDto} from "../../business";
import {useDetailLevelContext} from "../../components/business/DetailLevelContext.tsx";
import {PropertiesForm} from "../../components/layout/PropertiesForm.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";


export function EntityOverview({entity}: { entity: EntityDto }) {
  const {isDetailLevelTech} = useDetailLevelContext()
  return <PropertiesForm>
    {isDetailLevelTech && <div><Text>Entity&nbsp;code</Text></div>}
    {isDetailLevelTech && <div><Text><code>{entity.id}</code></Text></div>}
    <div><Text>From&nbsp;model</Text></div>
    <div><Link to="/model/$modelId" params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link>
    </div>
    <div><Text>Docs.&nbsp;home</Text></div>
    <div>{!entity.documentationHome ? <MissingInformation>add external link</MissingInformation> :
      <ExternalUrl url={entity.documentationHome}/>}</div>
    <div><Text>Tags</Text></div>
    <div>{entity.hashtags.length == 0 ? <MissingInformation>add tags</MissingInformation> :
      <Tags tags={entity.hashtags}/>}</div>
    <div><Text>Origin</Text></div>
    <div><Origin value={entity.origin}/></div>
  </PropertiesForm>
}