import {Link, useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  type AttributeDto,
  type EntityDto,
  Model,
  type RelationshipDto,
  useActionRegistry,
  useModel
} from "../../business";
import {ModelContext, useModelContext} from "../../components/business/ModelContext.tsx";
import {ViewTitle} from "../../components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Divider,
  Text,
  tokens
} from "@fluentui/react-components";
import {EntityIcon, ModelIcon, RelationshipIcon} from "../../components/business/Icons.tsx";
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {Markdown} from "../../components/core/Markdown.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {
  ContainedFixed,
  ContainedHeader,
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "../../components/layout/Contained.tsx";
import {SectionPaper} from "../../components/layout/SectionPaper.tsx";
import {
  createActionTemplateEntityAttribute,
  createActionTemplateModel,
  createActionTemplateRelationshipAttribute
} from "../../components/business/actionTemplates.ts";
import {useDetailLevelContext} from "../../components/business/DetailLevelContext.tsx";
import {PropertiesForm} from "../../components/layout/PropertiesForm.tsx";
import {Tags} from "../../components/core/Tag.tsx";
import {ErrorBox} from "@seij/common-ui";
import {toProblem} from "@seij/common-types";


export function AttributePage({modelId, parentType, parentId, attributeId}: {
  modelId: string,
  parentType: "entity" | "relationship",
  parentId: string,
  attributeId: string
}) {

  const {data: modelDto} = useModel(modelId)

  if (!modelDto) return <ErrorBox error={toProblem(`Can not find model with id [${modelId}]`)} />
  const model = new Model(modelDto)

  const entity = parentType === "entity" ? model.findEntityDto(parentId) : undefined
  const relationship = parentType === "relationship" ? model.findRelationshipDto(parentId) : undefined
  const parent = entity ?? relationship

  const attribute = entity
    ? model.findEntityAttributeDto(entity.id, attributeId)
    : relationship ? model.findRelationshipAttributeDto(relationship.id, attributeId)
      : undefined

  if (!parent) return <ErrorBox error={toProblem(`Can not find attribute [${attributeId}]'s parent parentType=[${parentType}] and parentId=[${parentId}]` )} />
  if (!attribute) return <ErrorBox error={toProblem(`Can not find attribute [${attributeId}] with parentType=[${parentType}] and parentId=[${parentId}]` )} />

  return <ModelContext value={model}>
    <AttributeView attribute={attribute} parent={parent} parentType={parentType}/>
  </ModelContext>
}

export function AttributeView({parent, parentType, attribute}: {
  parent: EntityDto | RelationshipDto,
  parentType: "entity" | "relationship",
  attribute: AttributeDto
}) {
  const model = useModelContext()
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const actions = actionRegistry.findActions(parentType == "entity" ? ActionUILocations.entity_attribute : ActionUILocations.relationship_attribute)

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id}
    })
  };

  const handleClickEntity = () => {
    navigate({
      to: "/model/$modelId/entity/$entityId",
      params: {modelId: model.id, entityId: parent.id}
    })
  };

  const handleClickRelationship = () => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: {modelId: model.id, relationshipId: parent.id}
    })
  };

  const parentAsEntity: EntityDto | null = parentType === "entity" ? (parent as EntityDto) : null
  const parentAsRelationship: RelationshipDto | null = parentType === "relationship" ? (parent as RelationshipDto) : null

  const actionParams = parentAsEntity !== null ? createActionTemplateEntityAttribute(model.id, parentAsEntity.id, attribute.id)
    : parentAsRelationship !== null ? createActionTemplateRelationshipAttribute(model.id, parentAsRelationship.id, attribute.id)
      : createActionTemplateModel(model.id)


  return <ViewLayoutContained title={<div>
    <div style={{marginLeft: "-22px"}}>
      <Breadcrumb size="small">
        <BreadcrumbItem>
          <BreadcrumbButton
            icon={<ModelIcon/>}
            onClick={handleClickModel}>{model.nameOrKey}</BreadcrumbButton></BreadcrumbItem>
        <BreadcrumbDivider/>
        {parentAsEntity != null &&
          <BreadcrumbItem>
            <BreadcrumbButton
              icon={<EntityIcon/>}
              onClick={handleClickEntity}>{parentAsEntity.name ?? parentAsEntity.id}</BreadcrumbButton>
          </BreadcrumbItem>
        }
        {parentAsRelationship != null &&
          <BreadcrumbItem>
            <BreadcrumbButton
              icon={<RelationshipIcon/>}
              onClick={handleClickRelationship}>{parentAsRelationship.name ?? parentAsRelationship.id}</BreadcrumbButton>
          </BreadcrumbItem>
        }

      </Breadcrumb>
    </div>
    <ViewTitle eyebrow={parentType === "entity" ? "Attribute of entity" : "Attribute of relationship"}>
      <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
        <div>{attribute.name ?? attribute.id} {" "}</div>
        <div>
          <ActionMenuButton
            label="Actions"
            itemActions={actions}
            actionParams={actionParams}/>
        </div>
      </div>
    </ViewTitle>
  </div>
  }>
    <ContainedMixedScrolling>
      <ContainedFixed>
        <ContainedHumanReadable>
          <ContainedHeader>

            <Divider/>
          </ContainedHeader>
        </ContainedHumanReadable>
      </ContainedFixed>

      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <AttributeOverview model={model} attribute={attribute}/>
          </SectionPaper>
          <SectionPaper topspacing="XXXL">
            {attribute.description ? <Markdown value={attribute.description}/> :
              <MissingInformation>add description</MissingInformation>}
          </SectionPaper>

        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

export function AttributeOverview({attribute, model}: {
  attribute: AttributeDto,
  model: Model
}) {
  const {isDetailLevelTech} = useDetailLevelContext()
  return <PropertiesForm>
    {isDetailLevelTech && <div><Text>Attribute&nbsp;code</Text></div>}
    {isDetailLevelTech && <div><Text><code>{attribute.id}</code></Text></div>}
    <div><Text>From&nbsp;model</Text></div>
    <div>
      <Link
        to="/model/$modelId"
        params={{modelId: model.id}}>{model.nameOrKey}</Link>
    </div>

    <div><Text>Tags</Text></div>
    <div>{attribute.hashtags.length == 0 ? <MissingInformation>add tags</MissingInformation> :
      <Tags tags={attribute.hashtags}/>}</div>

    <div><Text>Type</Text></div>
    <div>
      <Link
        to="/model/$modelId/type/$typeId"
        params={{modelId: model.id, typeId: attribute.type}}>{model.findTypeName(attribute.type)}</Link>
      {" "}
      <Text>{attribute.optional ? "optional" : "required"}</Text>
      {" "}
      <Text>{attribute.identifierAttribute ? "🔑 Identifier" : ""}</Text>
    </div>

  </PropertiesForm>
}
