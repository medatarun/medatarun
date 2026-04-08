import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  type EntityDto,
  Model,
  useEntityUpdateDescription,
  useEntityUpdateName,
  useModel,
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
} from "@fluentui/react-components";
import { AttributesTable } from "@/components/business/model/AttributesTable.tsx";
import { RelationshipsTable } from "@/components/business/model/RelationshipsTable.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";

import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { EntityOverview } from "./EntityOverview.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import {
  createActionTemplateEntity,
  createActionTemplateEntityAttribute,
  createActionTemplateEntityForRelationships,
  createDisplayedSubjectEntity,
} from "@/components/business/model/model.actions.ts";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import {
  AttributeIcon,
  EntityIcon,
  ModelIcon,
  RelationshipIcon,
} from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";

export function EntityEditPage({
  modelId,
  entityId,
}: {
  modelId: string;
  entityId: string;
}) {
  const { data: model } = useModel(modelId);

  const entity = model?.entities?.find((it) => it.id === entityId);
  if (!model) return null;
  if (!entity) return null;
  return (
    <ModelContext value={new Model(model)}>
      <EntityView entity={entity} />
    </ModelContext>
  );
}

export function EntityView({ entity }: { entity: EntityDto }) {
  const model = useModelContext();
  const entityUpdateDescription = useEntityUpdateDescription();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const entityUpdateName = useEntityUpdateName();
  const { t } = useAppI18n();

  const actions = actionRegistry.findActions(ActionUILocations.entity);
  const relationshipsInvolved = model.dto.relationships.filter((it) =>
    it.roles.some((r) => r.entityId === entity.id),
  );

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: { modelId: model.id },
    });
  };

  const handleClickAttribute = (attributeId: string) => {
    navigate({
      to: "/model/$modelId/entity/$entityId/attribute/$attributeId",
      params: {
        modelId: model.id,
        entityId: entity.id,
        attributeId: attributeId,
      },
    });
  };

  const handleClickRelationship = (relationshipId: string) => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: { modelId: model.id, relationshipId: relationshipId },
    });
  };

  const handleChangeName = (value: string) => {
    return entityUpdateName.mutateAsync({
      modelId: model.id,
      entityId: entity.id,
      value: value,
    });
  };
  const displayedSubject = createDisplayedSubjectEntity(model.id, entity.id);

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton icon={<ModelIcon />} onClick={handleClickModel}>
          {model.nameOrKeyWithAuthorityEmoji}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton current={true}>
          {t("entityEditPage_eyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadcrumb,
    title: (
      <InlineEditSingleLine
        value={entity.name ?? ""}
        onChange={handleChangeName}
      >
        {entity.name ? (
          model.findEntityNameOrKey(entity.id)
        ) : (
          <MissingInformation>
            {model.findEntityNameOrKey(entity.id)}
          </MissingInformation>
        )}{" "}
      </InlineEditSingleLine>
    ),
    titleIcon: <EntityIcon />,
    actions: {
      label: t("entityEditPage_actions"),
      itemActions: actions,
      actionParams: createActionTemplateEntity(model.id, entity.id),
      displayedSubject: displayedSubject,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <SectionPaper>
        <EntityOverview entity={entity} />
      </SectionPaper>
      <SectionPaper topspacing="XXXL" nopadding>
        <InlineEditDescription
          value={entity.description}
          placeholder={t("entityEditPage_descriptionPlaceholder")}
          onChange={(v) =>
            entityUpdateDescription.mutateAsync({
              modelId: model.id,
              entityId: entity.id,
              value: v,
            })
          }
        />
      </SectionPaper>

      <SectionTitle
        icon={<AttributeIcon />}
        actionParams={createActionTemplateEntity(model.id, entity.id)}
        displayedSubject={displayedSubject}
        location={ActionUILocations.entity_attributes}
      >
        {t("entityEditPage_attributesTitle")}
      </SectionTitle>

      <SectionTable>
        <AttributesTable
          attributes={entity.attributes}
          actionUILocation={ActionUILocations.entity_attribute}
          actionParamsFactory={(attributeId: string) =>
            createActionTemplateEntityAttribute(
              model.id,
              entity.id,
              attributeId,
            )
          }
          displayedSubject={displayedSubject}
          onClickAttribute={handleClickAttribute}
        />
      </SectionTable>

      <SectionTitle
        icon={<RelationshipIcon />}
        actionParams={createActionTemplateEntityForRelationships(
          model.id,
          entity.id,
        )}
        displayedSubject={displayedSubject}
        location={ActionUILocations.entity_relationships}
      >
        {t("entityEditPage_relationshipsTitle")}
      </SectionTitle>

      <SectionTable>
        <RelationshipsTable
          onClick={handleClickRelationship}
          relationships={relationshipsInvolved}
          displayedSubject={displayedSubject}
        />
      </SectionTable>

      <ViewLayoutTechnicalInfos
        id={entity.id}
        idLabel={t("entityEditPage_identifierLabel")}
        technicalKey={entity.key}
        keyLabel={t("entityEditPage_keyLabel")}
      ></ViewLayoutTechnicalInfos>
    </ViewLayoutContained>
  );
}
