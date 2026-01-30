import {useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  type ElementOrigin,
  Model,
  useActionRegistry,
  useModel,
  useModelAddTag,
  useModelDeleteTag,
  useModelUpdateDescription,
  useModelUpdateDocumentationHome,
  useModelUpdateKey,
  useModelUpdateName,
  useModelUpdateVersion
} from "../business";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {InfoLabel, Text, tokens} from "@fluentui/react-components";
import {EntityIcon, RelationshipIcon, TypeIcon} from "../components/business/Icons.tsx";
import {EntityCard} from "../components/business/EntityCard.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {ActionMenuButton, TypesTable} from "../components/business/TypesTable.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {useDetailLevelContext} from "../components/business/DetailLevelContext.tsx";
import {SectionTitle} from "../components/layout/SectionTitle.tsx";
import {MissingInformation} from "../components/core/MissingInformation.tsx";
import {ContainedHumanReadable, ContainedMixedScrolling, ContainedScrollable} from "../components/layout/Contained.tsx";
import {SectionPaper} from "../components/layout/SectionPaper.tsx";
import {SectionCards} from "../components/layout/SectionCards.tsx";
import {SectionTable} from "../components/layout/SecionTable.tsx";
import {PropertiesForm} from "../components/layout/PropertiesForm.tsx";
import {createActionTemplateModel} from "../components/business/actionTemplates.ts";
import {InlineEditDescription} from "../components/core/InlineEditDescription.tsx";
import {InlineEditSingleLine} from "../components/core/InlineEditSingleLine.tsx";
import {InlineEditTags} from "../components/core/InlineEditTags.tsx";


export function ModelPage({modelId}: { modelId: string }) {
  const {data: model} = useModel(modelId);

  return <div>
    {model && <ModelContext value={new Model(model)}><ModelView/></ModelContext>}
  </div>
}


export function ModelView() {
  const model = useModelContext().dto
  const actionRegistry = useActionRegistry()
  const navigate = useNavigate();
  const modelUpdateDescription = useModelUpdateDescription()
  const modelUpdateName = useModelUpdateName()


  const displayName = model.name ?? model.id

  const handleClickType = (typeId: string) => {
    navigate({to: "/model/$modelId/type/$typeId", params: {modelId: model.id, typeId: typeId}})
  }
  const handleClickRelationship = (relationshipId: string) => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: {modelId: model.id, relationshipId: relationshipId}
    })
  }
  const handleClickEntity = (entityId: string) => {
    navigate({
      to: "/model/$modelId/entity/$entityId",
      params: {modelId: model.id, entityId: entityId}
    })
  }

  const actions = actionRegistry.findActions(ActionUILocations.model_overview)

  const handleChangeName = (value: string) => {
    return modelUpdateName.mutateAsync({modelId: model.id, value: value})
  }


  return <ViewLayoutContained title={
    <div>
      <ViewTitle eyebrow={<span>Model</span>}>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div style={{width: "100%"}}>
            <InlineEditSingleLine
              value={model.name ?? ""}
              onChange={handleChangeName}>
              {displayName} {" "}
            </InlineEditSingleLine>
          </div>
          <div>
            <ActionMenuButton
              label="Actions"
              itemActions={actions}
              actionParams={createActionTemplateModel(model.id)}/>
          </div>
        </div>
      </ViewTitle>
    </div>
  }>
    <ContainedMixedScrolling>

      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <ModelOverview/>
          </SectionPaper>
          <SectionPaper topspacing="XXXL" nopadding>
            <InlineEditDescription
              value={model.description}
              placeholder={"add description"}
              onChange={v => modelUpdateDescription.mutateAsync({modelId: model.id, value: v})}
            />
          </SectionPaper>

          <SectionTitle
            icon={<EntityIcon/>}
            actionParams={createActionTemplateModel(model.id)}
            location={ActionUILocations.model_entities}>Entities</SectionTitle>

          {model.entities.length === 0 && <p><MissingInformation>add entities</MissingInformation></p>}
          {model.entities.length > 0 && <SectionCards><EntitiesCardList onClick={handleClickEntity}/></SectionCards>}

          <SectionTitle
            icon={<RelationshipIcon/>}
            actionParams={createActionTemplateModel(model.id)}
            location={ActionUILocations.model_relationships}>Relationships</SectionTitle>

          {model.relationships.length === 0 && <p><MissingInformation>add relationships</MissingInformation></p>}
          {model.relationships.length > 0 && <SectionTable><RelationshipsTable onClick={handleClickRelationship}
                                                                               relationships={model.relationships}/></SectionTable>}

          <SectionTitle
            icon={<TypeIcon/>}
            actionParams={createActionTemplateModel(model.id)}
            location={ActionUILocations.model_types}>Data Types</SectionTitle>

          {model.types.length === 0 && <p><MissingInformation>add data types</MissingInformation></p>}
          {model.types.length > 0 &&
            <SectionTable><TypesTable onClick={handleClickType} types={model.types}/></SectionTable>}

        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

export function ModelOverview() {

  const model = useModelContext().dto
  const {isDetailLevelTech} = useDetailLevelContext()
  const modelUpdateVersion = useModelUpdateVersion()
  const modelUpdateKey = useModelUpdateKey()
  const modelUpdateDocumentationHome = useModelUpdateDocumentationHome()
  const modelUpdateAddTag = useModelAddTag()
  const modelUpdateDeleteTag = useModelDeleteTag()

  const handleChangeVersion = (value: string) => {
    return modelUpdateVersion.mutateAsync({modelId: model.id, value: value})
  }
  const handleChangeKey = (value: string) => {
    return modelUpdateKey.mutateAsync({modelId: model.id, value: value})
  }
  const handleChangeDocumentationHome = (value: string) => {
    return modelUpdateDocumentationHome.mutateAsync({modelId: model.id, value: value})
  }
  const handleChangeTags = async (value: string[]) => {
    for (const tag of model.hashtags) {
      if (!value.includes(tag)) await modelUpdateDeleteTag.mutateAsync({modelId: model.id, tag: tag})
    }
    for (const tag of value) {
      if (!model.hashtags.includes(tag)) await modelUpdateAddTag.mutateAsync({modelId: model.id, tag: tag})
    }
  }
  return <PropertiesForm>
    {isDetailLevelTech && <div><InfoLabel>Model&nbsp;key</InfoLabel></div>}
    {isDetailLevelTech && <div>
      <InlineEditSingleLine
        value={model.key}
        onChange={handleChangeKey}>
        <Text><code>{model.key}</code></Text>
      </InlineEditSingleLine>
    </div>}

    <div>Version</div>
    <div>
      <InlineEditSingleLine
        value={model.version}
        onChange={handleChangeVersion}>
        <code>{model.version}</code>
      </InlineEditSingleLine>
    </div>

    <div>External&nbsp;link</div>
    <div>
      <InlineEditSingleLine
        value={model.documentationHome ?? ""}
        onChange={handleChangeDocumentationHome}
      >{!model.documentationHome
        ? <MissingInformation>add external link</MissingInformation>
        : <ExternalUrl url={model.documentationHome}/>}
      </InlineEditSingleLine>
    </div>

    <div>Tags</div>
    <div>
      <InlineEditTags value={model.hashtags} onChange={handleChangeTags}>
        {
          model.hashtags.length === 0
            ? <MissingInformation>add tags</MissingInformation>
            : <Tags tags={model.hashtags}/>
        }
      </InlineEditTags>
    </div>
    {isDetailLevelTech && <div>Origin</div>}
    {isDetailLevelTech && <div><Origin value={model.origin}/></div>}
    {isDetailLevelTech && <div>Identifier</div>}
    {isDetailLevelTech && <div><code>{model.id}</code></div>}
  </PropertiesForm>


}

export function EntitiesCardList({onClick}: { onClick: (entityId: string) => void }) {
  const model = useModelContext()
  const entities = model.dto.entities
  return <div>
    <div style={{
      display: "flex",
      columnGap: tokens.spacingHorizontalM,
      rowGap: tokens.spacingVerticalM,
      paddingTop: tokens.spacingVerticalM,
      justifyContent: "left",
      flexWrap: "wrap"
    }}>
      {
        entities.map(entity => <EntityCard
          key={entity.id}
          entity={entity}
          onClick={() => {
            onClick(entity.id)
          }}/>
        )
      }
    </div>
  </div>
}

export function Origin({
                         value
                       }: {
  value: ElementOrigin
}) {
  if (value.type == "manual") return "Medatarun (manual)"
  return <ExternalUrl url={value.uri}/>
}

export function ExternalUrl({
                              url
                            }: {
  url: string | null
}) {
  if (!url) return null
  return <a href={url} target="_blank">{url}</a>;
}


