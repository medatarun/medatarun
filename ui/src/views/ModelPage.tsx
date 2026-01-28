import {useNavigate} from "@tanstack/react-router";
import {ActionUILocations, type ElementOrigin, Model, useActionRegistry, useModel} from "../business";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {tokens} from "@fluentui/react-components";
import {EntityIcon, RelationshipIcon, TypeIcon} from "../components/business/Icons.tsx";
import {EntityCard} from "../components/business/EntityCard.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {ActionMenuButton, TypesTable} from "../components/business/TypesTable.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {useDetailLevelContext} from "../components/business/DetailLevelContext.tsx";
import {SectionTitle} from "../components/layout/SectionTitle.tsx";
import {Markdown} from "../components/core/Markdown.tsx";
import {MissingInformation} from "../components/core/MissingInformation.tsx";
import {ContainedHumanReadable, ContainedMixedScrolling, ContainedScrollable} from "../components/layout/Contained.tsx";
import {SectionPaper} from "../components/layout/SectionPaper.tsx";
import {SectionCards} from "../components/layout/SectionCards.tsx";
import {SectionTable} from "../components/layout/SecionTable.tsx";
import {PropertiesForm} from "../components/layout/PropertiesForm.tsx";
import {createActionTemplateModel} from "../components/business/actionTemplates.ts";

export function ModelPage({modelId}: { modelId: string }) {
  const {data: model} = useModel(modelId);
  return <div>
    {model && <ModelContext value={new Model(model)}><ModelView/></ModelContext>}
  </div>
}


export function ModelView() {
  const model = useModelContext().dto
  const actionRegistry = useActionRegistry()

  const displayName = model.name ?? model.id
  const navigate = useNavigate();

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
  return <ViewLayoutContained title={
    <div>
      <ViewTitle eyebrow={<span>Model</span>}>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div>{displayName} {" "}</div>
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
          <SectionPaper topspacing="XXXL">
            {model.description ? <Markdown value={model.description}/> :
              <MissingInformation>add description</MissingInformation>}
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
  return <PropertiesForm>
    {isDetailLevelTech && <div>Identifier</div>}
    {isDetailLevelTech && <div><code>{model.id}</code></div>}
    <div>Version</div>
    <div><code>{model.version}</code></div>
    <div>Documentation</div>
    <div>{!model.documentationHome ? <MissingInformation>add external link</MissingInformation> :
      <ExternalUrl url={model.documentationHome}/>}</div>
    <div>Hashtags</div>
    <div>{model.hashtags.length === 0 ? <MissingInformation>add tags</MissingInformation> :
      <Tags tags={model.hashtags}/>}</div>
    {isDetailLevelTech && <div>Origin</div>}
    {isDetailLevelTech && <div><Origin value={model.origin}/></div>}
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
