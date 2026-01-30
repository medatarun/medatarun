import {Link, useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  type AttributeDto,
  type EntityDto,
  Model,
  type RelationshipDto,
  useActionRegistry,
  useEntityAttributeAddTag,
  useEntityAttributeDeleteTag,
  useEntityAttributeUpdateDescription,
  useEntityAttributeUpdateKey,
  useEntityAttributeUpdateName,
  useEntityAttributeUpdateOptional,
  useEntityAttributeUpdateType,
  useModel,
  useRelationshipAttributeAddTag,
  useRelationshipAttributeDeleteTag,
  useRelationshipAttributeUpdateDescription,
  useRelationshipAttributeUpdateKey,
  useRelationshipAttributeUpdateName,
  useRelationshipAttributeUpdateOptional,
  useRelationshipAttributeUpdateType
} from "../../business";
import {ModelContext, useModelContext} from "../../components/business/ModelContext.tsx";
import {ViewTitle} from "../../components/core/ViewTitle.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
  tokens
} from "@fluentui/react-components";
import {EntityIcon, ModelIcon, RelationshipIcon} from "../../components/business/Icons.tsx";
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {
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
import {InlineEditDescription} from "../../components/core/InlineEditDescription.tsx";
import {InlineEditSingleLine} from "../../components/core/InlineEditSingleLine.tsx";
import {InlineEditTags} from "../../components/core/InlineEditTags.tsx";


export function AttributePage({modelId, parentType, parentId, attributeId}: {
  modelId: string,
  parentType: "entity" | "relationship",
  parentId: string,
  attributeId: string
}) {

  const {data: modelDto} = useModel(modelId)

  if (!modelDto) return <ErrorBox error={toProblem(`Can not find model with id [${modelId}]`)}/>
  const model = new Model(modelDto)

  const entity = parentType === "entity" ? model.findEntityDto(parentId) : undefined
  const relationship = parentType === "relationship" ? model.findRelationshipDto(parentId) : undefined
  const parent = entity ?? relationship

  const attribute = entity
    ? model.findEntityAttributeDto(entity.id, attributeId)
    : relationship ? model.findRelationshipAttributeDto(relationship.id, attributeId)
      : undefined

  if (!parent) return <ErrorBox
    error={toProblem(`Can not find attribute [${attributeId}]'s parent parentType=[${parentType}] and parentId=[${parentId}]`)}/>
  if (!attribute) return <ErrorBox
    error={toProblem(`Can not find attribute [${attributeId}] with parentType=[${parentType}] and parentId=[${parentId}]`)}/>

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

  const entityAttributeUpdateName = useEntityAttributeUpdateName()
  const entityAttributeUpdateDescription = useEntityAttributeUpdateDescription()

  const relationshipAttributeUpdateName = useRelationshipAttributeUpdateName()
  const relationshipAttributeUpdateDescription = useRelationshipAttributeUpdateDescription()


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

  const actionParams = parentAsEntity !== null
    ? createActionTemplateEntityAttribute(model.id, parentAsEntity.id, attribute.id)
    : parentAsRelationship !== null
      ? createActionTemplateRelationshipAttribute(model.id, parentAsRelationship.id, attribute.id)
      : createActionTemplateModel(model.id)

  const handleUpdateName = async (value: string) => {
    if (parentAsEntity != null) {
      return entityAttributeUpdateName.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value
      })
    } else if (parentAsRelationship != null) {
      return relationshipAttributeUpdateName.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value
      })
    }
  }

  const handleUpdateDescription = async (value: string) => {
    if (parentAsEntity != null) {
      return entityAttributeUpdateDescription.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value
      })
    } else if (parentAsRelationship != null) {
      return relationshipAttributeUpdateDescription.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value
      })
    }
    throw new Error("Attribute has neither an entity or a relationship as parent.")
  }


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
        <div style={{width: "100%"}}>
          <InlineEditSingleLine
            value={attribute.name ?? ""}
            onChange={handleUpdateName}>
            {attribute.name ? attribute.name : <MissingInformation>{attribute.key}</MissingInformation>} {" "}
          </InlineEditSingleLine>
        </div>
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
      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <AttributeOverview
              model={model}
              attribute={attribute}
              parentAsEntity={parentAsEntity}
              parentAsRelationship={parentAsRelationship}
            />
          </SectionPaper>
          <SectionPaper topspacing="XXXL" nopadding>
            <InlineEditDescription
              value={attribute.description}
              placeholder={"add description"}
              onChange={handleUpdateDescription}
            />
          </SectionPaper>

        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

export function AttributeOverview({attribute, model, parentAsEntity, parentAsRelationship}: {
  attribute: AttributeDto,
  model: Model,
  parentAsEntity: EntityDto | null,
  parentAsRelationship: RelationshipDto | null,
}) {
  const {isDetailLevelTech} = useDetailLevelContext()

  const entityAttributeUpdateKey = useEntityAttributeUpdateKey()
  const entityAttributeUpdateType = useEntityAttributeUpdateType()
  const entityAttributeAddTag = useEntityAttributeAddTag()
  const entityAttributeDeleteTag = useEntityAttributeDeleteTag()
  const entityAttributeUpdateOptional = useEntityAttributeUpdateOptional()

  const relationshipAttributeUpdateKey = useRelationshipAttributeUpdateKey()
  const relationshipAttributeUpdateType = useRelationshipAttributeUpdateType()
  const relationshipAttributeAddTag = useRelationshipAttributeAddTag()
  const relationshipAttributeDeleteTag = useRelationshipAttributeDeleteTag()
  const relationshipAttributeUpdateOptional = useRelationshipAttributeUpdateOptional()

  const handleChangeKey = (value: string) => {
    if (parentAsEntity) {
      return entityAttributeUpdateKey.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value
      })
    } else if (parentAsRelationship) {
      return relationshipAttributeUpdateKey.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value
      })
    } else {
      throw toProblem("Attribute is neither a relationship attribute or an entity attribute")
    }
  }

  const handleChangeType = (value: string) => {
    if (parentAsEntity) {
      return entityAttributeUpdateType.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value
      })
    } else if (parentAsRelationship) {
      return relationshipAttributeUpdateType.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value
      })
    } else {
      throw toProblem("Attribute is neither a relationship attribute or an entity attribute")
    }
  }

  const handleChangeRequired = (value: string) => {
    if (parentAsEntity) {
      return entityAttributeUpdateOptional.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value !== "true"
      })
    } else if (parentAsRelationship) {
      return relationshipAttributeUpdateOptional.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value !== "true"
      })
    } else {
      throw toProblem("Attribute is neither a relationship attribute or an entity attribute")
    }
  }

  const handleChangeTags = async (value: string[]) => {
    for (const tag of attribute.hashtags) {
      if (!value.includes(tag)) {
        if (parentAsEntity) {
          await entityAttributeDeleteTag.mutateAsync({
            modelId: model.id,
            entityId: parentAsEntity.id,
            attributeId: attribute.id,
            tag: tag
          })
        } else if (parentAsRelationship) {
          await relationshipAttributeDeleteTag.mutateAsync({
            modelId: model.id,
            relationshipId: parentAsRelationship.id,
            attributeId: attribute.id,
            tag: tag
          })
        }
      } else {
        throw toProblem("Attribute is neither a relationship attribute or an entity attribute")
      }
    }
    for (const tag of value) {
      if (!attribute.hashtags.includes(tag)) {
        if (parentAsEntity) {
          await entityAttributeAddTag.mutateAsync({
            modelId: model.id,
            entityId: parentAsEntity.id,
            attributeId: attribute.id,
            tag: tag
          })
        } else if (parentAsRelationship) {
          await relationshipAttributeAddTag.mutateAsync({
            modelId: model.id,
            relationshipId: parentAsRelationship.id,
            attributeId: attribute.id,
            tag: tag
          })
        } else {
          throw toProblem("Attribute is neither a relationship attribute or an entity attribute")
        }
      }
    }
  }

  return <PropertiesForm>
    {isDetailLevelTech && <div><Text>Attribute&nbsp;code</Text></div>}
    {isDetailLevelTech && <div>
      <InlineEditSingleLine value={attribute.key} onChange={handleChangeKey}>
        <Text><code>{attribute.key}</code></Text>
      </InlineEditSingleLine>
    </div>}
    <div><Text>From&nbsp;model</Text></div>
    <div>
      <Link
        to="/model/$modelId"
        params={{modelId: model.id}}>{model.nameOrKey}</Link>
    </div>

    <div><Text>Tags</Text></div>
    <div><InlineEditTags value={attribute.hashtags} onChange={handleChangeTags}>
      {
        attribute.hashtags.length === 0
          ? <MissingInformation>add tags</MissingInformation>
          : <Tags tags={attribute.hashtags}/>
      }
    </InlineEditTags></div>

    <div><Text>Type</Text></div>
    <div>
      <InlineEditSingleLine value={attribute.type} onChange={handleChangeType}>
      <Link
        to="/model/$modelId/type/$typeId"
        params={{modelId: model.id, typeId: attribute.type}}>{model.findTypeNameOrKey(attribute.type)}</Link>
      {" "}
      <Text>{attribute.identifierAttribute ? "🔑 Identifier" : ""}</Text>
      </InlineEditSingleLine>
    </div>

    <div><Text>Required</Text></div>
    <div>
      <InlineEditSingleLine value={attribute.optional ? "false":"true"} onChange={handleChangeRequired}>
      <Text>{attribute.optional ? "Not required" : "Yes, required"}</Text>
      </InlineEditSingleLine>
    </div>

  </PropertiesForm>
}
