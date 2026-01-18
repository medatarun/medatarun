import {useNavigate} from "@tanstack/react-router";
import {type ElementOrigin, Model, useActionRegistry, useModel} from "../business";
import {ModelContext, useModelContext} from "../components/business/ModelContext.tsx";
import {Tags} from "../components/core/Tag.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Divider,
  tokens
} from "@fluentui/react-components";
import {EntityIcon, ModelIcon, RelationshipIcon, TypeIcon} from "../components/business/Icons.tsx";
import {EntityCard} from "../components/business/EntityCard.tsx";
import {RelationshipsTable} from "../components/business/RelationshipsTable.tsx";
import {ActionMenuButton, TypesTable} from "../components/business/TypesTable.tsx";
import {ViewLayoutContained} from "../components/layout/ViewLayoutContained.tsx";
import {ViewTitle} from "../components/core/ViewTitle.tsx";
import {useDetailLevelContext} from "../components/business/DetailLevelContext.tsx";
import {SectionTitle} from "../components/layout/SectionTitle.tsx";
import {Markdown} from "../components/core/Markdown.tsx";
import {MissingInformation} from "../components/core/MissingInformation.tsx";
import {
  ContainedFixed,
  ContainedHeader,
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "../components/layout/Contained.tsx";
import {SectionPaper} from "../components/layout/SectionPaper.tsx";
import {SectionCards} from "../components/layout/SectionCards.tsx";
import {SectionTable} from "../components/layout/SecionTable.tsx";
import {PropertiesForm} from "../components/layout/PropertiesForm.tsx";

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
  const handleClickModels = () => {
    navigate({to: "/models"})
  }
  const handleClickType = (typeId: string) => {
    navigate({to: "/model/$modelId/type/$typeId", params: {modelId: model.id, typeId: typeId}})
  }
  const handleClickRelationship = (relationshipId: string) => {
    navigate({
      to: "/model/$modelKey/relationship/$relationshipKey",
      params: {modelKey: model.id, relationshipKey: relationshipId}
    })
  }
  const handleClickEntity = (entityId: string) => {
    navigate({
      to: "/model/$modelId/entityDef/$entityDefId",
      params: {modelId: model.id, entityDefId: entityId}
    })
  }

  const actions = actionRegistry.findActions("model.overview")

  return <ViewLayoutContained title={
    <Breadcrumb>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon/>}
                                        onClick={handleClickModels}>Models</BreadcrumbButton></BreadcrumbItem>
      <BreadcrumbDivider/>
      <BreadcrumbItem><BreadcrumbButton icon={<ModelIcon/>} current>{displayName}</BreadcrumbButton></BreadcrumbItem>
    </Breadcrumb>
  }>
    <ContainedMixedScrolling>
      <ContainedFixed>
        <ContainedHumanReadable>
          <ContainedHeader>
            <ViewTitle eyebrow={<span><ModelIcon/> Model</span>}>
              {displayName} {" "}
              <ActionMenuButton
                itemActions={actions}
                actionParams={{modelKey: model.id}}/>
            </ViewTitle>
            <Divider/>
          </ContainedHeader>
        </ContainedHumanReadable>
      </ContainedFixed>

      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <ModelOverview/>
          </SectionPaper>
          <SectionPaper topspacing="XXXL">
            {model.description ? <Markdown value={model.description}/> :
              <MissingInformation>No description provided.</MissingInformation>}
          </SectionPaper>

          <SectionTitle
            icon={<EntityIcon/>}
            actionParams={{modelKey: model.id}}
            location="model.entities">Entities</SectionTitle>

          <SectionCards><EntitiesCardList onClick={handleClickEntity}/></SectionCards>

          <SectionTitle
            icon={<RelationshipIcon/>}
            actionParams={{modelKey: model.id}}
            location="model.relationships">Relationships</SectionTitle>

          <SectionTable>
            <RelationshipsTable onClick={handleClickRelationship}  relationships={model.relationshipDefs}/>
          </SectionTable>

          <SectionTitle
            icon={<TypeIcon/>}
            actionParams={{modelKey: model.id}}
            location="model.types">Types</SectionTitle>

          <SectionTable>
            <TypesTable onClick={handleClickType} types={model.types}/>
          </SectionTable>

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
    <div>{!model.documentationHome ? <MissingInformation>Not provided.</MissingInformation> :
      <ExternalUrl url={model.documentationHome}/>}</div>
    <div>Hashtags</div>
    <div>{model.hashtags.length === 0 ? <MissingInformation>Not tagged.</MissingInformation> :
      <Tags tags={model.hashtags}/>}</div>
    {isDetailLevelTech && <div>Origin</div>}
    {isDetailLevelTech && <div><Origin value={model.origin}/></div>}
  </PropertiesForm>


}

export function EntitiesCardList({onClick}: { onClick: (entityId: string) => void }) {
  const model = useModelContext()
  const entities = model.dto.entityDefs
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
        entities.map(entityDef => <EntityCard
          key={entityDef.id}
          entity={entityDef}
          onClick={() => {
            onClick(entityDef.id)
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
