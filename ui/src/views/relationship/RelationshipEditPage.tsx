import { useActionRegistry } from "@/business/action_registry";
import {
  type AttributeDto,
  createActionCtxRelationship,
  createActionCtxRelationshipAttribute,
  createActionCtxRelationshipRole,
  createDisplayedSubjectRelationship,
  Model,
  type RelationshipDto,
  type RelationshipRoleDto,
  useModel,
  useRelationshipAddTag,
  useRelationshipDeleteTag,
  useRelationshipUpdateDescription,
  useRelationshipUpdateKey,
  useRelationshipUpdateName,
} from "@/business/model";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { AttributesTable } from "@/components/business/model/AttributesTable.tsx";
import {
  AttributeIcon,
  EntityIcon,
  ModelIcon,
  RelationshipIcon,
} from "@/components/business/model/model.icons.tsx";
import { ModelContext } from "@/components/business/model/ModelContext.tsx";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import { Key } from "@/components/core/Key";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionCards } from "@/components/layout/SectionCards.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos";
import { type AppMessageKey, useAppI18n } from "@/services/appI18n.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Caption1,
  Card,
  CardHeader,
  Text,
  tokens,
} from "@fluentui/react-components";
import { toProblem } from "@seij/common-types";
import { ErrorBox } from "@seij/common-ui";
import { useNavigate } from "@tanstack/react-router";
import { useSecurityContext } from "@/business/security";

export function RelationshipEditPage({
  modelId,
  relationshipId,
}: {
  modelId: string;
  relationshipId: string;
}) {
  const { t } = useAppI18n();
  const { data: modelDto } = useModel(modelId);

  if (!modelDto) return null;
  const model = new Model(modelDto);

  const relationship = model.findRelationshipDto(relationshipId);
  if (!relationship)
    return <ErrorBox error={toProblem(t("relationshipEditPage_notFound"))} />;

  return (
    <ModelContext value={model}>
      <RelationshipView model={model} relationship={relationship} />
    </ModelContext>
  );
}

export function RelationshipView({
  model,
  relationship,
}: {
  model: Model;
  relationship: RelationshipDto;
}) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActionDescriptors([
    "relationship_update_key",
    "relationship_delete",
  ]);
  const { isDetailLevelTech } = useDetailLevelContext();
  const relationshipUpdateDescription = useRelationshipUpdateDescription();
  const updateName = useRelationshipUpdateName();
  const sec = useSecurityContext();

  const updateNameDisabled = !sec.canExecuteAction("relationship_update_name");
  const updateDescriptionDisabled = !sec.canExecuteAction(
    "relationship_update_description",
  );

  const handleChangeName = (value: string) => {
    return updateName.mutateAsync({
      modelId: model.id,
      relationshipId: relationship.id,
      value: value,
    });
  };

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: { modelId: model.id },
    });
  };

  const handleClickAttribute = (attributeId: string) => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId/attribute/$attributeId",
      params: {
        modelId: model.id,
        relationshipId: relationship.id,
        attributeId: attributeId,
      },
    });
  };

  const displayedSubject = createDisplayedSubjectRelationship(
    model.id,
    relationship.id,
  );
  const actionCtxPage = createActionCtxRelationship(
    model,
    relationship,
    displayedSubject,
  );

  const actionCtxAttribute = (attr: AttributeDto) =>
    createActionCtxRelationshipAttribute(
      model,
      relationship,
      attr,
      displayedSubject,
    );

  const actionCtxRole = (role: RelationshipRoleDto) =>
    createActionCtxRelationshipRole(
      model,
      relationship,
      role,
      displayedSubject,
    );

  const breadCrumb = (
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
        <BreadcrumbButton
          icon={<ModelIcon authority={model.authority} />}
          current={true}
        >
          {t("relationshipEditPage_eyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadCrumb,
    title: (
      <InlineEditSingleLine
        value={relationship.name ?? ""}
        disabled={updateNameDisabled}
        onChange={handleChangeName}
      >
        {relationship.name ? (
          model.findRelationshipNameOrKey(relationship.id)
        ) : (
          <span
            style={{
              color: tokens.colorNeutralForeground4,
              fontStyle: "italic",
            }}
          >
            {model.findRelationshipNameOrKey(relationship.id)}
          </span>
        )}{" "}
      </InlineEditSingleLine>
    ),
    titleIcon: <RelationshipIcon />,
    actions: {
      label: t("relationshipEditPage_actions"),
      itemActions: actions,
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      scrollable={true}
      contained={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <SectionPaper>
        <RelationshipOverview model={model} relationship={relationship} />
      </SectionPaper>
      <SectionPaper topspacing="XXXL" nopadding>
        <InlineEditDescription
          value={relationship.description}
          disabled={updateDescriptionDisabled}
          placeholder={t("relationshipEditPage_descriptionPlaceholder")}
          onChange={(v) =>
            relationshipUpdateDescription.mutateAsync({
              modelId: model.id,
              relationshipId: relationship.id,
              value: v,
            })
          }
        />
      </SectionPaper>

      <SectionTitle
        icon={<AttributeIcon />}
        actionCtx={actionCtxPage}
        actions={["relationship_role_create"]}
      >
        {t("relationshipEditPage_rolesTitle")}
      </SectionTitle>

      {relationship.roles.length === 0 && (
        <p>
          <MissingInformation>
            {t("relationshipEditPage_rolesEmpty")}
          </MissingInformation>
        </p>
      )}
      {relationship.roles.length > 0 && (
        <SectionCards>
          <div
            style={{
              display: "flex",
              columnGap: tokens.spacingHorizontalM,
              rowGap: tokens.spacingVerticalM,
              paddingTop: tokens.spacingVerticalM,
              justifyContent: "left",
              flexWrap: "wrap",
            }}
          >
            {relationship.roles.map((role) => (
              <Card style={{ width: "30%" }} key={role.id}>
                <CardHeader
                  style={{ height: "2em" }}
                  header={
                    <div>
                      <div>
                        <Text weight="semibold">
                          <div>
                            {roleCardinalityLabel(role.cardinality, t)}{" "}
                            {role.name ?? <Key value={role.key} />}
                          </div>
                          {role.name && isDetailLevelTech && (
                            <Caption1>
                              <Key value={role.key} />
                            </Caption1>
                          )}
                        </Text>
                      </div>
                    </div>
                  }
                  action={
                    <ActionMenuButton
                      itemActions={actionRegistry.findActionDescriptors([
                        "relationship_role_update_name",
                        "relationship_role_update_key",
                        "relationship_role_update_entity",
                        "relationship_role_update_cardinality",
                        "relationship_role_delete",
                      ])}
                      actionCtx={actionCtxRole(role)}
                    />
                  }
                />
                <div>
                  <EntityIcon /> {model.findEntityNameOrKey(role.entityId)}
                </div>
              </Card>
            ))}
          </div>
        </SectionCards>
      )}

      <SectionTitle
        icon={<AttributeIcon />}
        actionCtx={actionCtxPage}
        actions={["relationship_attribute_create"]}
      >
        {t("relationshipEditPage_attributesTitle")}
      </SectionTitle>

      {relationship.attributes.length === 0 && (
        <p>
          <MissingInformation>
            {t("relationshipEditPage_attributesEmpty")}
          </MissingInformation>
        </p>
      )}
      {relationship.attributes.length > 0 && (
        <SectionTable>
          <AttributesTable
            attributes={relationship.attributes}
            actions={["relationship_attribute_delete"]}
            actionCtxAttribute={actionCtxAttribute}
            parentId={relationship.id}
            onClickAttribute={handleClickAttribute}
          />
        </SectionTable>
      )}
      <ViewLayoutTechnicalInfos
        id={relationship.id}
        idLabel={t("relationshipEditPage_identifierLabel")}
        technicalKey={relationship.key}
        keyLabel={t("relationshipEditPage_keyLabel")}
      />
    </ViewLayoutContained>
  );
}

export function RelationshipOverview({
  relationship,
  model,
}: {
  relationship: RelationshipDto;
  model: Model;
}) {
  const { t } = useAppI18n();
  const { isDetailLevelTech } = useDetailLevelContext();
  const relationshipUpdateKey = useRelationshipUpdateKey();
  const relationshipAddTag = useRelationshipAddTag();
  const relationshipDeleteTag = useRelationshipDeleteTag();
  const sec = useSecurityContext();

  const updateKeyDisabled = !sec.canExecuteAction("relationship_update_key");
  const updateTagDisabled = !sec.canExecuteActions(
    "relationship_add_tag",
    "relationship_delete_tag",
  );
  const handleChangeKey = (value: string) => {
    return relationshipUpdateKey.mutateAsync({
      modelId: model.id,
      relationshipId: relationship.id,
      value: value,
    });
  };

  const handleChangeTags = async (value: string[]) => {
    for (const tag of relationship.tags) {
      if (!value.includes(tag))
        await relationshipDeleteTag.mutateAsync({
          modelId: model.id,
          relationshipId: relationship.id,
          tag: "id:" + tag,
        });
    }
    for (const tag of value) {
      if (!relationship.tags.includes(tag))
        await relationshipAddTag.mutateAsync({
          modelId: model.id,
          relationshipId: relationship.id,
          tag: "id:" + tag,
        });
    }
  };
  return (
    <PropertiesForm>
      {isDetailLevelTech && (
        <div>
          <Text>{t("relationshipEditPage_keyLabel")}</Text>
        </div>
      )}

      {isDetailLevelTech && (
        <InlineEditSingleLine
          value={relationship.key}
          disabled={updateKeyDisabled}
          onChange={handleChangeKey}
        >
          <Text>
            <code>{relationship.key}</code>
          </Text>
        </InlineEditSingleLine>
      )}
      <div>
        <Text>{t("relationshipEditPage_tagsLabel")}</Text>
      </div>
      <div>
        <InlineEditTags
          value={relationship.tags}
          scope={modelTagScope(model.id)}
          disabled={updateTagDisabled}
          onChange={handleChangeTags}
          displayedSubject={createDisplayedSubjectRelationship(
            model.id,
            relationship.id,
          )}
        >
          {relationship.tags.length === 0 ? (
            <MissingInformation>
              {t("relationshipEditPage_tagsEmpty")}
            </MissingInformation>
          ) : (
            <Tags tags={relationship.tags} scope={modelTagScope(model.id)} />
          )}
        </InlineEditTags>
      </div>
    </PropertiesForm>
  );
}

function roleCardinalityLabel(
  c: RelationshipRoleDto["cardinality"],
  t: (key: AppMessageKey, values?: Record<string, unknown>) => string,
) {
  if (c === "zeroOrOne") return t("relationshipEditPage_cardinalityMaybeOne");
  if (c === "many") return t("relationshipEditPage_cardinalityMany");
  if (c === "one") return t("relationshipEditPage_cardinalityOne");
  if (c === "unknown") return t("relationshipEditPage_cardinalityUnknown");
}
