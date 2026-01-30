import {Text} from "@fluentui/react-components";
import {ExternalUrl, Origin} from "../ModelPage.tsx";
import {Link} from "@tanstack/react-router";
import {Tags} from "../../components/core/Tag.tsx";
import {
  type EntityDto,
  useEntityAddTag,
  useEntityDeleteTag,
  useEntityUpdateDocumentationHome,
  useEntityUpdateKey
} from "../../business";
import {useDetailLevelContext} from "../../components/business/DetailLevelContext.tsx";
import {PropertiesForm} from "../../components/layout/PropertiesForm.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {InlineEditSingleLine} from "../../components/core/InlineEditSingleLine.tsx";
import {useModelContext} from "../../components/business/ModelContext.tsx";
import {InlineEditTags} from "../../components/core/InlineEditTags.tsx";


export function EntityOverview({entity}: { entity: EntityDto }) {
  const model = useModelContext()
  const {isDetailLevelTech} = useDetailLevelContext()
  const entityUpdateKey = useEntityUpdateKey()
  const entityUpdateDocumentationHome = useEntityUpdateDocumentationHome()
  const entityAddTag = useEntityAddTag()
  const entityDeleteTag = useEntityDeleteTag()

  const handleChangeKey = (value: string) => {
    return entityUpdateKey.mutateAsync({modelId: model.id, entityId: entity.id, value: value})
  }
  const handleChangeDocumentationHome = (value: string) => {
    return entityUpdateDocumentationHome.mutateAsync({modelId: model.id, entityId: entity.id, value: value})
  }
  const handleChangeTags = async (value: string[]) => {
    for (const tag of entity.hashtags) {
      if (!value.includes(tag)) await entityDeleteTag.mutateAsync({modelId: model.id, entityId: entity.id, tag: tag})
    }
    for (const tag of value) {
      if (!entity.hashtags.includes(tag)) await entityAddTag.mutateAsync({
        modelId: model.id,
        entityId: entity.id,
        tag: tag
      })
    }
  }
  return <PropertiesForm>
    {isDetailLevelTech && <div><Text>Entity&nbsp;key</Text></div>}
    {isDetailLevelTech && <div>
      <InlineEditSingleLine value={entity.key} onChange={handleChangeKey}>
        <Text><code>{entity.key}</code></Text>
      </InlineEditSingleLine>
    </div>}
    <div><Text>From&nbsp;model</Text></div>
    <div><Link to="/model/$modelId" params={{modelId: entity.model.id}}>{entity.model.name ?? entity.model.id}</Link>
    </div>

    <div><Text>External&nbsp;link</Text></div>
    <div><InlineEditSingleLine
      value={entity.documentationHome ?? ""}
      onChange={handleChangeDocumentationHome}
    >{!entity.documentationHome
      ? <MissingInformation>add external link</MissingInformation>
      : <ExternalUrl url={entity.documentationHome}/>}
    </InlineEditSingleLine>
    </div>

    <div><Text>Tags</Text></div>
    <div>
      <InlineEditTags value={entity.hashtags} onChange={handleChangeTags}>
        {
          entity.hashtags.length === 0
            ? <MissingInformation>add tags</MissingInformation>
            : <Tags tags={entity.hashtags}/>
        }
      </InlineEditTags>
    </div>

    <div><Text>Origin</Text></div>
    <div><Origin value={entity.origin}/></div>
    {isDetailLevelTech && <div><Text>Identifier</Text></div>}
    {isDetailLevelTech && <div><Text><code>{entity.id}</code></Text></div>}

  </PropertiesForm>
}