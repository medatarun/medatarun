import { Link, useNavigate } from "@tanstack/react-router";
import { useActionRegistry } from "@/business/action_registry";
import {
  type AttributeDto,
  createActionCtxEntityAttribute,
  createActionCtxRelationshipAttribute,
  createDisplayedSubjectEntityAttribute,
  createDisplayedSubjectRelationshipAttribute,
  type EntityDto,
  Model,
  type RelationshipDto,
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
  useRelationshipAttributeUpdateType,
} from "@/business/model";
import {
  ModelContext,
  useModelContext,
} from "@/components/business/model/ModelContext.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
} from "@fluentui/react-components";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import {
  AttributeIcon,
  EntityIcon,
  ModelIcon,
  RelationshipIcon,
} from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  type ActionDisplayedSubject,
  createActionCtxVoid,
  displaySubjectNone,
} from "@/components/business/actions";
import { InlineEditBoolean } from "@/components/core/InlineEditBoolean.tsx";
import { InlineEditCombobox } from "@/components/core/InlineEditCombobox.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import { useSecurityContext } from "@/business/security";

export function AttributeEditPage({
  modelId,
  parentType,
  parentId,
  attributeId,
}: {
  modelId: string;
  parentType: "entity" | "relationship";
  parentId: string;
  attributeId: string;
}) {
  const { t } = useAppI18n();
  const { data: modelDto } = useModel(modelId);

  if (!modelDto)
    return (
      <ErrorBox
        error={toProblem(t("attributeEditPage_modelNotFound", { modelId }))}
      />
    );
  const model = new Model(modelDto);

  const entity =
    parentType === "entity" ? model.findEntityDto(parentId) : undefined;
  const relationship =
    parentType === "relationship"
      ? model.findRelationshipDto(parentId)
      : undefined;
  const parent = entity ?? relationship;

  const attribute = entity
    ? model.findEntityAttributeDto(entity.id, attributeId)
    : relationship
      ? model.findRelationshipAttributeDto(relationship.id, attributeId)
      : undefined;

  if (!parent)
    return (
      <ErrorBox
        error={toProblem(
          t("attributeEditPage_parentNotFound", {
            attributeId,
            parentType,
            parentId,
          }),
        )}
      />
    );
  if (!attribute)
    return (
      <ErrorBox
        error={toProblem(
          t("attributeEditPage_attributeNotFound", {
            attributeId,
            parentType,
            parentId,
          }),
        )}
      />
    );

  return (
    <ModelContext value={model}>
      <AttributeView
        attribute={attribute}
        parent={parent}
        parentType={parentType}
      />
    </ModelContext>
  );
}

export function AttributeView({
  parent,
  parentType,
  attribute,
}: {
  parent: EntityDto | RelationshipDto;
  parentType: "entity" | "relationship";
  attribute: AttributeDto;
}) {
  const model = useModelContext();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();

  const entityAttributeUpdateName = useEntityAttributeUpdateName();
  const entityAttributeUpdateDescription =
    useEntityAttributeUpdateDescription();

  const relationshipAttributeUpdateName = useRelationshipAttributeUpdateName();
  const relationshipAttributeUpdateDescription =
    useRelationshipAttributeUpdateDescription();
  const { t } = useAppI18n();
  const sec = useSecurityContext();

  const actions = actionRegistry.findActionDescriptors(
    parentType == "entity"
      ? ["entity_attribute_delete"]
      : ["relationship_attribute_delete"],
  );

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: { modelId: model.id },
    });
  };

  const handleClickEntity = () => {
    navigate({
      to: "/model/$modelId/entity/$entityId",
      params: { modelId: model.id, entityId: parent.id },
    });
  };

  const handleClickRelationship = () => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: { modelId: model.id, relationshipId: parent.id },
    });
  };

  const parentAsEntity: EntityDto | null =
    parentType === "entity" ? (parent as EntityDto) : null;

  const parentAsRelationship: RelationshipDto | null =
    parentType === "relationship" ? (parent as RelationshipDto) : null;

  const updateNameDisabled = parentAsEntity
    ? !sec.canExecuteAction("entity_attribute_update_name")
    : !sec.canExecuteAction("relationship_attribute_update_name");

  const updateDescriptionDisabled = parentAsEntity
    ? !sec.canExecuteAction("entity_attribute_update_description")
    : !sec.canExecuteAction("relationship_attribute_update_description");

  const displayedSubject: ActionDisplayedSubject =
    parentAsEntity !== null
      ? createDisplayedSubjectEntityAttribute(
          model.id,
          parentAsEntity.id,
          attribute.id,
        )
      : parentAsRelationship != null
        ? createDisplayedSubjectRelationshipAttribute(
            model.id,
            parentAsRelationship.id,
            attribute.id,
          )
        : displaySubjectNone;

  const actionCtxPage =
    parentAsEntity != null
      ? createActionCtxEntityAttribute(
          model,
          parentAsEntity,
          attribute,
          displayedSubject,
        )
      : parentAsRelationship != null
        ? createActionCtxRelationshipAttribute(
            model,
            parentAsRelationship,
            attribute,
            displayedSubject,
          )
        : createActionCtxVoid();

  const handleUpdateName = async (value: string) => {
    if (parentAsEntity != null) {
      return entityAttributeUpdateName.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value,
      });
    } else if (parentAsRelationship != null) {
      return relationshipAttributeUpdateName.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value,
      });
    }
  };

  const handleUpdateDescription = async (value: string) => {
    if (parentAsEntity != null) {
      return entityAttributeUpdateDescription.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value,
      });
    } else if (parentAsRelationship != null) {
      return relationshipAttributeUpdateDescription.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value,
      });
    }
    throw new Error(
      "Attribute has neither an entity or a relationship as parent.",
    );
  };

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton icon={<ModelIcon />} onClick={handleClickModel}>
          {model.nameOrKeyWithAuthorityEmoji}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      {parentAsEntity != null && (
        <BreadcrumbItem>
          <BreadcrumbButton icon={<EntityIcon />} onClick={handleClickEntity}>
            {model.findEntityNameOrKey(parentAsEntity.id)}
          </BreadcrumbButton>
        </BreadcrumbItem>
      )}
      {parentAsRelationship != null && (
        <BreadcrumbItem>
          <BreadcrumbButton
            icon={<RelationshipIcon />}
            onClick={handleClickRelationship}
          >
            {model.findRelationshipNameOrKey(parentAsRelationship.id)}
          </BreadcrumbButton>
        </BreadcrumbItem>
      )}
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton icon={<AttributeIcon />} current={true}>
          {parentType === "entity"
            ? t("attributeEditPage_entityEyebrow")
            : t("attributeEditPage_relationshipEyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadcrumb,
    title: (
      <InlineEditSingleLine
        value={attribute.name ?? ""}
        onChange={handleUpdateName}
        disabled={updateNameDisabled}
      >
        {attribute.name ? (
          attribute.name
        ) : (
          <MissingInformation>{attribute.key}</MissingInformation>
        )}{" "}
      </InlineEditSingleLine>
    ),
    titleIcon: <AttributeIcon />,
    actions: {
      label: t("attributeEditPage_actions"),
      itemActions: actions,
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
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
          placeholder={t("attributeEditPage_descriptionPlaceholder")}
          disabled={updateDescriptionDisabled}
          onChange={handleUpdateDescription}
        />
      </SectionPaper>
      <ViewLayoutTechnicalInfos
        id={attribute.id}
        idLabel={t("attributeEditPage_identifierLabel")}
        technicalKey={attribute.key}
        keyLabel={t("attributeEditPage_keyLabel")}
      />
    </ViewLayoutContained>
  );
}

export function AttributeOverview({
  attribute,
  model,
  parentAsEntity,
  parentAsRelationship,
}: {
  attribute: AttributeDto;
  model: Model;
  parentAsEntity: EntityDto | null;
  parentAsRelationship: RelationshipDto | null;
}) {
  const { isDetailLevelTech } = useDetailLevelContext();
  const { t } = useAppI18n();

  const entityAttributeUpdateKey = useEntityAttributeUpdateKey();
  const entityAttributeUpdateType = useEntityAttributeUpdateType();
  const entityAttributeAddTag = useEntityAttributeAddTag();
  const entityAttributeDeleteTag = useEntityAttributeDeleteTag();
  const entityAttributeUpdateOptional = useEntityAttributeUpdateOptional();

  const relationshipAttributeUpdateKey = useRelationshipAttributeUpdateKey();
  const relationshipAttributeUpdateType = useRelationshipAttributeUpdateType();
  const relationshipAttributeAddTag = useRelationshipAttributeAddTag();
  const relationshipAttributeDeleteTag = useRelationshipAttributeDeleteTag();
  const relationshipAttributeUpdateOptional =
    useRelationshipAttributeUpdateOptional();

  const sec = useSecurityContext();

  const isPKParticipant =
    parentAsEntity &&
    model.isEntityAttributePK(parentAsEntity.id, attribute.id);

  const updateKeyDisabled = parentAsEntity
    ? !sec.canExecuteAction("entity_attribute_update_key")
    : !sec.canExecuteAction("relationship_attribute_update_key");

  const updateTypeDisabled = parentAsEntity
    ? !sec.canExecuteAction("entity_attribute_update_type")
    : !sec.canExecuteAction("relationship_attribute_update_type");

  const updateTagDisabled = parentAsEntity
    ? !sec.canExecuteActions(
        "entity_attribute_add_tag",
        "entity_attribute_delete_tag",
      )
    : !sec.canExecuteActions(
        "relationship_attribute_add_tag",
        "relationship_attribute_delete_tag",
      );

  const updateOptionalDisabled = parentAsEntity
    ? !sec.canExecuteAction("entity_attribute_update_optional")
    : !sec.canExecuteAction("relationship_attribute_update_optional");

  const handleChangeKey = (value: string) => {
    if (parentAsEntity) {
      return entityAttributeUpdateKey.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: value,
      });
    } else if (parentAsRelationship) {
      return relationshipAttributeUpdateKey.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: value,
      });
    } else {
      throw toProblem(
        "Attribute is neither a relationship attribute or an entity attribute",
      );
    }
  };

  const handleChangeType = (value: string) => {
    if (parentAsEntity) {
      return entityAttributeUpdateType.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: "id:" + value,
      });
    } else if (parentAsRelationship) {
      return relationshipAttributeUpdateType.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: "id:" + value,
      });
    } else {
      throw toProblem(
        "Attribute is neither a relationship attribute or an entity attribute",
      );
    }
  };

  const handleChangeRequired = (value: boolean) => {
    if (parentAsEntity) {
      return entityAttributeUpdateOptional.mutateAsync({
        modelId: model.id,
        entityId: parentAsEntity.id,
        attributeId: attribute.id,
        value: !value,
      });
    } else if (parentAsRelationship) {
      return relationshipAttributeUpdateOptional.mutateAsync({
        modelId: model.id,
        relationshipId: parentAsRelationship.id,
        attributeId: attribute.id,
        value: !value,
      });
    } else {
      throw toProblem(
        "Attribute is neither a relationship attribute or an entity attribute",
      );
    }
  };

  const handleChangeTags = async (value: string[]) => {
    for (const tag of attribute.tags) {
      if (!value.includes(tag)) {
        if (parentAsEntity) {
          await entityAttributeDeleteTag.mutateAsync({
            modelId: model.id,
            entityId: parentAsEntity.id,
            attributeId: attribute.id,
            tag: "id:" + tag,
          });
        } else if (parentAsRelationship) {
          await relationshipAttributeDeleteTag.mutateAsync({
            modelId: model.id,
            relationshipId: parentAsRelationship.id,
            attributeId: attribute.id,
            tag: "id:" + tag,
          });
        } else {
          throw toProblem(
            "Attribute is neither a relationship attribute or an entity attribute",
          );
        }
      }
    }
    for (const tag of value) {
      if (!attribute.tags.includes(tag)) {
        if (parentAsEntity) {
          await entityAttributeAddTag.mutateAsync({
            modelId: model.id,
            entityId: parentAsEntity.id,
            attributeId: attribute.id,
            tag: "id:" + tag,
          });
        } else if (parentAsRelationship) {
          await relationshipAttributeAddTag.mutateAsync({
            modelId: model.id,
            relationshipId: parentAsRelationship.id,
            attributeId: attribute.id,
            tag: "id:" + tag,
          });
        } else {
          throw toProblem(
            "Attribute is neither a relationship attribute or an entity attribute",
          );
        }
      }
    }
  };

  const attributeDisplayedSubject: ActionDisplayedSubject = parentAsEntity
    ? createDisplayedSubjectEntityAttribute(
        model.id,
        parentAsEntity.id,
        attribute.id,
      )
    : parentAsRelationship
      ? createDisplayedSubjectRelationshipAttribute(
          model.id,
          parentAsRelationship.id,
          attribute.id,
        )
      : displaySubjectNone;
  const typeOptions = model.findTypeOptions();

  return (
    <PropertiesForm>
      <div>
        <Text>{t("attributeEditPage_typeLabel")}</Text>
      </div>
      <div>
        <InlineEditCombobox
          value={attribute.type}
          options={typeOptions}
          disabled={updateTypeDisabled}
          placeholder={t("attributeEditPage_typeLabel")}
          onChange={handleChangeType}
        >
          <Link
            to="/model/$modelId/type/$typeId"
            params={{ modelId: model.id, typeId: attribute.type }}
          >
            {model.findTypeNameOrKey(attribute.type)}
          </Link>{" "}
        </InlineEditCombobox>
      </div>

      {isPKParticipant && (
        <div>
          <Text>{t("attributeEditPage_primaryKeyLabel")}</Text>
        </div>
      )}
      {isPKParticipant && (
        <div>
          <Text>🔑 {t("attributeEditPage_primaryKeyYes")}</Text>
        </div>
      )}

      <div>
        <Text>{t("attributeEditPage_requiredLabel")}</Text>
      </div>
      <div>
        <InlineEditBoolean
          value={!attribute.optional}
          labelTrue={t("attributeEditPage_requiredYes")}
          labelFalse={t("attributeEditPage_requiredNo")}
          disabled={updateOptionalDisabled}
          onChange={handleChangeRequired}
        >
          <Text>
            {attribute.optional
              ? t("attributeEditPage_requiredNo")
              : t("attributeEditPage_requiredYes")}
          </Text>
        </InlineEditBoolean>
      </div>

      {isDetailLevelTech && (
        <div>
          <Text>{t("attributeEditPage_keyLabel")}</Text>
        </div>
      )}

      {isDetailLevelTech && (
        <div>
          <InlineEditSingleLine
            value={attribute.key}
            disabled={updateKeyDisabled}
            onChange={handleChangeKey}
          >
            <Text>
              <code>{attribute.key}</code>
            </Text>
          </InlineEditSingleLine>
        </div>
      )}
      <div>
        <Text>{t("attributeEditPage_tagsLabel")}</Text>
      </div>
      <div>
        <InlineEditTags
          value={attribute.tags}
          scope={modelTagScope(model.id)}
          disabled={updateTagDisabled}
          onChange={handleChangeTags}
          displayedSubject={attributeDisplayedSubject}
        >
          {attribute.tags.length === 0 ? (
            <MissingInformation>
              {t("attributeEditPage_tagsEmpty")}
            </MissingInformation>
          ) : (
            <Tags tags={attribute.tags} scope={modelTagScope(model.id)} />
          )}
        </InlineEditTags>
      </div>
    </PropertiesForm>
  );
}
