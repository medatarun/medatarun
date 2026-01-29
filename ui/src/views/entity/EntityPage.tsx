import {useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  type EntityDto,
  Model,
  useActionRegistry,
  useEntityUpdateDescription,
  useEntityUpdateName,
  useModel
} from "../../business";
import {ModelContext, useModelContext} from "../../components/business/ModelContext.tsx";
import {ViewTitle} from "../../components/core/ViewTitle.tsx";
import {Breadcrumb, BreadcrumbButton, BreadcrumbDivider, BreadcrumbItem, tokens} from "@fluentui/react-components";
import {AttributeIcon, ModelIcon, RelationshipIcon} from "../../components/business/Icons.tsx";
import {AttributesTable} from "../../components/business/AttributesTable.tsx";
import {RelationshipsTable} from "../../components/business/RelationshipsTable.tsx";
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";

import {SectionTitle} from "../../components/layout/SectionTitle.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {EntityOverview} from "./EntityOverview.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "../../components/layout/Contained.tsx";
import {SectionPaper} from "../../components/layout/SectionPaper.tsx";
import {SectionTable} from "../../components/layout/SecionTable.tsx";
import {
  createActionTemplateEntity,
  createActionTemplateEntityAttribute,
  createActionTemplateEntityForRelationships
} from "../../components/business/actionTemplates.ts";
import {InlineEditDescription} from "../../components/core/InlineEditDescription.tsx";
import {InlineEditSingleLine} from "../../components/core/InlineEditSingleLine.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";


export function EntityPage({modelId, entityId}: { modelId: string, entityId: string }) {

  const {data: model} = useModel(modelId)

  const entity = model?.entities?.find(it => it.id === entityId)
  if (!model) return null
  if (!entity) return null
  return <ModelContext value={new Model(model)}><EntityView entity={entity}/></ModelContext>
}

export function EntityView({entity}: { entity: EntityDto }) {

  const model = useModelContext()
  const entityUpdateDescription= useEntityUpdateDescription()
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const entityUpdateName = useEntityUpdateName()

  const actions = actionRegistry.findActions(ActionUILocations.entity)
  const relationshipsInvolved = model.dto.relationships
    .filter(it => it.roles.some(r => r.entityId === entity.id));

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id}
    })
  };

  const handleClickAttribute = (attributeId: string) => {
    navigate({
      to: "/model/$modelId/entity/$entityId/attribute/$attributeId",
      params: {modelId: model.id, entityId: entity.id, attributeId: attributeId}
    })
  }

  const handleClickRelationship = (relationshipId: string) => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: {modelId: model.id, relationshipId: relationshipId}
    })
  }

  const handleChangeName = (value: string) => {
    return entityUpdateName.mutateAsync({modelId: model.id, entityId: entity.id, value: value})
  }
  return <ViewLayoutContained title={
    <div>
      <Breadcrumb style={{marginLeft: "-22px"}} size="small">
        <BreadcrumbItem>
          <BreadcrumbButton
            icon={<ModelIcon/>}
            onClick={handleClickModel}>{model.nameOrKey}</BreadcrumbButton></BreadcrumbItem>
        <BreadcrumbDivider/>
      </Breadcrumb>
      <ViewTitle eyebrow={"Entity"}>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div style={{width: "100%"}}>
            <InlineEditSingleLine
              value={entity.name ?? ""}
              onChange={handleChangeName}>
              {entity.name ?              model.findEntityNameOrKey(entity.id) : <MissingInformation>{model.findEntityNameOrKey(entity.id)}</MissingInformation>} {" "}
            </InlineEditSingleLine>
          </div>
          <div><ActionMenuButton
            label="Actions"
            itemActions={actions}
            actionParams={createActionTemplateEntity(model.id, entity.id)}/></div>
        </div>
      </ViewTitle>

    </div>
  }>
    <ContainedMixedScrolling>
      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <EntityOverview entity={entity}/>
          </SectionPaper>
          <SectionPaper topspacing="XXXL" nopadding>
            <InlineEditDescription
              value={entity.description}
              placeholder={"add description"}
              onChange = {v => entityUpdateDescription.mutateAsync({modelId: model.id, entityId: entity.id, value: v})}
            />
          </SectionPaper>

          <SectionTitle
            icon={<AttributeIcon/>}
            actionParams={createActionTemplateEntity(model.id, entity.id)}
            location={ActionUILocations.entity_attributes}>Attributes</SectionTitle>

          <SectionTable>
            <AttributesTable
              attributes={entity.attributes}
              actionUILocation={ActionUILocations.entity_attribute}
              actionParamsFactory={(attributeId:string)=>createActionTemplateEntityAttribute(model.id, entity.id, attributeId)}
              onClickAttribute={handleClickAttribute}/>
          </SectionTable>

          <SectionTitle
            icon={<RelationshipIcon/>}
            actionParams={createActionTemplateEntityForRelationships(model.id, entity.id)}
            location={ActionUILocations.entity_relationships}>Relationships</SectionTitle>

          <SectionTable>
            <RelationshipsTable
              onClick={handleClickRelationship}
              relationships={relationshipsInvolved}/>
          </SectionTable>

        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

