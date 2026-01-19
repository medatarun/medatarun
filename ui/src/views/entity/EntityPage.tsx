import {useNavigate} from "@tanstack/react-router";
import {ActionUILocations, type EntityDto, Model, useActionRegistry, useModel} from "../../business";
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
import {Markdown} from "../../components/core/Markdown.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "../../components/layout/Contained.tsx";
import {SectionPaper} from "../../components/layout/SectionPaper.tsx";
import {SectionTable} from "../../components/layout/SecionTable.tsx";


export function EntityPage({modelId, entityDefId}: { modelId: string, entityDefId: string }) {

  const {data: model} = useModel(modelId)

  const entity = model?.entityDefs?.find(it => it.id === entityDefId)
  if (!model) return null
  if (!entity) return null
  return <ModelContext value={new Model(model)}><EntityView entity={entity}/></ModelContext>
}

export function EntityView({entity}: { entity: EntityDto }) {
  const model = useModelContext()
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions(ActionUILocations.entity)
  const relationshipsInvolved = model.dto.relationshipDefs
    .filter(it => it.roles.some(r => r.entityId === entity.id));

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id}
    })
  };

  const handleClickAttribute = (attributeId: string) => {
    navigate({
      to: "/model/$modelKey/entity/$entityKey/attribute/$attributeKey",
      params: {modelKey: model.id, entityKey: entity.id, attributeKey: attributeId}
    })
  }

  const handleClickRelationship = (relationshipId: string) => {
    navigate({
      to: "/model/$modelKey/relationship/$relationshipKey",
      params: {modelKey: model.id, relationshipKey: relationshipId}
    })
  }

  return <ViewLayoutContained title={
    <div>
      <Breadcrumb style={{marginLeft: "-22px"}} size="small">
        <BreadcrumbItem>
          <BreadcrumbButton
            icon={<ModelIcon/>}
            onClick={handleClickModel}>{model.nameOrId}</BreadcrumbButton></BreadcrumbItem>
        <BreadcrumbDivider/>
      </Breadcrumb>
      <ViewTitle eyebrow={"Entity"}>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div>{entity.name ?? entity.id} {" "}</div>
          <div><ActionMenuButton
            label="Actions"
            itemActions={actions}
            actionParams={{
              modelKey: model.id,
              entityKey: entity.id
            }}/></div>
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
          <SectionPaper topspacing="XXXL">
            {entity.description ? <Markdown value={entity.description}/> :
              <MissingInformation>No description provided.</MissingInformation>}
          </SectionPaper>

          <SectionTitle
            icon={<AttributeIcon/>}
            actionParams={{modelKey: model.id, entityKey: entity.id}}
            location={ActionUILocations.entity_attributes}>Attributes</SectionTitle>

          <SectionTable>
            <AttributesTable entityId={entity.id} attributes={entity.attributes}
                             onClickAttribute={handleClickAttribute}/>
          </SectionTable>

          <SectionTitle
            icon={<RelationshipIcon/>}
            actionParams={{modelKey: model.id, roleAEntityKey: entity.id}}
            location={ActionUILocations.entity_relationships}>Relationships</SectionTitle>

          <SectionTable>
            <RelationshipsTable onClick={handleClickRelationship} relationships={relationshipsInvolved}/>
          </SectionTable>

        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

