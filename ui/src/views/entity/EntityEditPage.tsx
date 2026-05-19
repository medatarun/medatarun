import { useNavigate } from "@tanstack/react-router";
import {
  type AttributeDto,
  type BusinessKeyDto,
  createActionCtxBusinessKey,
  createActionCtxEntity,
  createActionCtxEntityAttribute,
  createActionCtxRelationship,
  createDisplayedSubjectEntity,
  type EntityDto,
  Model,
  type RelationshipDto,
} from "@medatarun/ui/business/model";
import {
  ModelContext,
  useModelContext,
} from "@medatarun/ui/components/business/model/ModelContext.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Table,
  TableBody,
  TableCell,
  TableRow,
  tokens,
} from "@fluentui/react-components";
import { AttributesTable } from "@medatarun/ui/components/business/model/AttributesTable.tsx";
import { RelationshipsTable } from "@medatarun/ui/components/business/model/RelationshipsTable.tsx";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";

import { SectionTitle } from "@medatarun/ui/components/layout/SectionTitle.tsx";
import { EntityOverview } from "./EntityOverview.tsx";
import { SectionTable } from "@medatarun/ui/components/layout/SecionTable.tsx";
import { InlineEditDescription } from "@medatarun/ui/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@medatarun/ui/components/core/InlineEditSingleLine.tsx";
import { MissingInformation } from "@medatarun/ui/components/core/MissingInformation.tsx";
import {
  AttributeIcon,
  EntityIcon,
  ModelIcon,
  RelationshipIcon,
} from "@medatarun/ui/components/business/model/model.icons.tsx";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutTechnicalInfos } from "@medatarun/ui/components/layout/ViewLayoutTechnicalInfos.tsx";
import { useSecurityContext } from "@medatarun/ui/components/business/security";
import { KeyRegular } from "@fluentui/react-icons";
import { Key } from "@medatarun/ui/components/core/Key.tsx";
import { MarkdownSummary } from "@medatarun/ui/components/core/MarkdownSummary.tsx";
import { ActionMenuButton } from "@medatarun/ui/components/business/actions/ActionMenuButton.tsx";
import { useDetailLevelContext } from "@medatarun/ui/components/business/detail-level";
import { useActionRegistry } from "@medatarun/ui/components/business/actions";
import {
  useEntityUpdateDescription,
  useEntityUpdateName,
  useModel,
} from "@medatarun/ui/components/business/model";
import { SectionSeparator } from "@medatarun/ui/components/layout/SectionSeparator.tsx";

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
  const sec = useSecurityContext();
  const { isDetailLevelTech } = useDetailLevelContext();
  const { t } = useAppI18n();

  const actions = actionRegistry.findActionDescriptors([
    "models/entity_primary_key_update",
    "models/entity_delete",
  ]);
  const relationshipsInvolved = model.dto.relationships.filter((it) =>
    it.roles.some((r) => r.entityId === entity.id),
  );

  const canChangeName = sec.canExecuteAction("models/entity_update_name");
  const canEditDescription = sec.canExecuteAction(
    "models/entity_update_description",
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

  const actionCtxPage = createActionCtxEntity(model, entity, displayedSubject);

  const actionCtxRelationship = (r: RelationshipDto) =>
    createActionCtxRelationship(model, r, displayedSubject);

  const actionCtxAttribute = (attr: AttributeDto) =>
    createActionCtxEntityAttribute(model, entity, attr, displayedSubject);

  const actionCtxBusinessKey = (bk: BusinessKeyDto) =>
    createActionCtxBusinessKey(model, entity, bk, displayedSubject);

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<ModelIcon authority={model.authority} />}
          onClick={handleClickModel}
        >
          {model.nameOrKey}
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
        disabled={!canChangeName}
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
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      verticalSpacing={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <EntityOverview entity={entity} displayedSubject={displayedSubject} />
      <InlineEditDescription
        value={entity.description}
        placeholder={t("entityEditPage_descriptionPlaceholder")}
        disabled={!canEditDescription}
        onChange={(v) =>
          entityUpdateDescription.mutateAsync({
            modelId: model.id,
            entityId: entity.id,
            value: v,
          })
        }
      />

      <SectionSeparator />

      <SectionTitle
        icon={<AttributeIcon />}
        actionCtx={actionCtxPage}
        actions={[
          "models/entity_attribute_create",
          "models/entity_primary_key_update",
        ]}
      >
        {t("entityEditPage_attributesTitle")}
      </SectionTitle>

      <SectionTable>
        <AttributesTable
          attributes={entity.attributes}
          actions={["models/entity_attribute_delete"]}
          parentId={entity.id}
          onClickAttribute={handleClickAttribute}
          actionCtxAttribute={actionCtxAttribute}
        />
      </SectionTable>

      <SectionTitle
        icon={<RelationshipIcon />}
        actionCtx={actionCtxPage}
        actions={["models/relationship_create"]}
      >
        {t("entityEditPage_relationshipsTitle")}
      </SectionTitle>

      <SectionTable>
        <RelationshipsTable
          onClick={handleClickRelationship}
          relationships={relationshipsInvolved}
          actionCtxRelationship={actionCtxRelationship}
        />
      </SectionTable>

      <SectionTitle
        icon={<KeyRegular />}
        actionCtx={actionCtxPage}
        actions={["models/business_key_create"]}
      >
        {t("entityEditPage_businessKeysTitle")}
      </SectionTitle>

      <SectionTable>
        <Table>
          <TableBody>
            {model.findBusinessKeysByEntityId(entity.id).map((bk) => {
              const participantsJoined = bk.participants
                .map((it) => model.findEntityAttributeNameOrKey(entity.id, it))
                .join(", ");
              return (
                <TableRow
                  key={bk.id}
                  style={{ border: "1px solid " + tokens.colorNeutralStroke2 }}
                >
                  <TableCell>
                    <div>{bk.name ?? <Key value={bk.key} />}</div>
                    {bk.name && isDetailLevelTech && (
                      <div>
                        <Key value={bk.key} />
                      </div>
                    )}
                    {bk.description && (
                      <MarkdownSummary value={bk.description} maxChars={150} />
                    )}
                  </TableCell>
                  <TableCell>
                    {bk.participants.length > 0 ? (
                      participantsJoined
                    ) : (
                      <MissingInformation>
                        {t("entityEditPage_businessKeyNoParticipant")}
                      </MissingInformation>
                    )}
                  </TableCell>
                  <TableCell
                    style={{
                      paddingTop: tokens.spacingVerticalM,
                      paddingBottom: tokens.spacingVerticalM,
                      width: "3em",
                      verticalAlign: "baseline",
                      textAlign: "right",
                    }}
                  >
                    <ActionMenuButton
                      itemActions={actionRegistry.findActionDescriptors([
                        "models/business_key_update_name",
                        "models/business_key_update_description",
                        "models/business_key_update_key",
                        "models/business_key_update_participants",
                        "models/business_key_delete",
                      ])}
                      actionCtx={actionCtxBusinessKey(bk)}
                    />
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
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
