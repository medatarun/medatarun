import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
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
import { ModelContext } from "@/components/business/model/ModelContext.tsx";
import { Link, useNavigate } from "@tanstack/react-router";
import {
  createActionTemplateRelationship,
  createActionTemplateRelationshipAttribute,
  createActionTemplateRelationshipRole,
} from "@/components/business/model/model.actions.ts";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Card,
  CardHeader,
  Text,
  tokens,
} from "@fluentui/react-components";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { AttributesTable } from "@/components/business/model/AttributesTable.tsx";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import { SectionCards } from "@/components/layout/SectionCards.tsx";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import {
  AttributeIcon,
  ModelIcon,
} from "@/components/business/model/model.icons.tsx";
import { useAppI18n, type AppMessageKey } from "@/services/appI18n.tsx";

export function RelationshipPage({
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
    return <ErrorBox error={toProblem(t("relationshipPage_notFound"))} />;

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
  const actions = actionRegistry.findActions(ActionUILocations.relationship);
  const { isDetailLevelTech } = useDetailLevelContext();
  const relationshipUpdateDescription = useRelationshipUpdateDescription();
  const updateName = useRelationshipUpdateName();

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

  const actionParams = createActionTemplateRelationship(
    model.id,
    relationship.id,
  );

  return (
    <ViewLayoutContained
      title={
        <div>
          <div style={{ marginLeft: "-22px" }}>
            <Breadcrumb size="small">
              <BreadcrumbItem>
                <BreadcrumbButton
                  icon={<ModelIcon />}
                  onClick={handleClickModel}
                >
                  {model.nameOrKey}
                </BreadcrumbButton>
              </BreadcrumbItem>
              <BreadcrumbDivider />
            </Breadcrumb>
          </div>
          <div>
            <ViewTitle eyebrow={t("relationshipPage_eyebrow")}>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  paddingRight: tokens.spacingHorizontalL,
                }}
              >
                <div style={{ width: "100%" }}>
                  <InlineEditSingleLine
                    value={relationship.name ?? ""}
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
                </div>
                <div>
                  <ActionMenuButton
                    label={t("relationshipPage_actions")}
                    itemActions={actions}
                    actionParams={actionParams}
                  />
                </div>
              </div>
            </ViewTitle>
          </div>
        </div>
      }
    >
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <ContainedHumanReadable>
            <SectionPaper>
              <RelationshipOverview model={model} relationship={relationship} />
            </SectionPaper>
            <SectionPaper topspacing="XXXL" nopadding>
              <InlineEditDescription
                value={relationship.description}
                placeholder={t("relationshipPage_descriptionPlaceholder")}
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
              actionParams={createActionTemplateRelationship(
                model.id,
                relationship.id,
              )}
              location={ActionUILocations.relationship_roles}
            >
              {t("relationshipPage_rolesTitle")}
            </SectionTitle>

            {relationship.roles.length === 0 && (
              <p>
                <MissingInformation>
                  {t("relationshipPage_rolesEmpty")}
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
                                {model.findEntityNameOrKey(role.entityId)}
                              </Text>
                            </div>
                            <Text>
                              {roleCardinalityLabel(role.cardinality, t)}
                            </Text>
                          </div>
                        }
                        action={
                          <ActionMenuButton
                            itemActions={actionRegistry.findActions(
                              ActionUILocations.relationship_role,
                            )}
                            actionParams={createActionTemplateRelationshipRole(
                              model.id,
                              relationship.id,
                              role.id,
                            )}
                          />
                        }
                      />
                      <div>{role.name}</div>
                      {isDetailLevelTech && (
                        <div>
                          <code>{role.key}</code>
                        </div>
                      )}
                      {isDetailLevelTech && (
                        <div>
                          🔗 <code>{model.findEntityKey(role.entityId)}</code>
                        </div>
                      )}
                    </Card>
                  ))}
                </div>
              </SectionCards>
            )}

            <SectionTitle
              icon={<AttributeIcon />}
              actionParams={createActionTemplateRelationship(
                model.id,
                relationship.id,
              )}
              location={ActionUILocations.relationship_attributes}
            >
              {t("relationshipPage_attributesTitle")}
            </SectionTitle>

            {relationship.attributes.length === 0 && (
              <p>
                <MissingInformation>
                  {t("relationshipPage_attributesEmpty")}
                </MissingInformation>
              </p>
            )}
            {relationship.attributes.length > 0 && (
              <SectionTable>
                <AttributesTable
                  attributes={relationship.attributes}
                  actionUILocation={ActionUILocations.relationship_attribute}
                  actionParamsFactory={(attributeId: string) =>
                    createActionTemplateRelationshipAttribute(
                      model.id,
                      relationship.id,
                      attributeId,
                    )
                  }
                  onClickAttribute={handleClickAttribute}
                />
              </SectionTable>
            )}
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
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
          <Text>{t("relationshipPage_keyLabel")}</Text>
        </div>
      )}

      {isDetailLevelTech && (
        <InlineEditSingleLine
          value={relationship.key}
          onChange={handleChangeKey}
        >
          <Text>
            <code>{relationship.key}</code>
          </Text>
        </InlineEditSingleLine>
      )}
      <div>
        <Text>{t("relationshipPage_fromModelLabel")}</Text>
      </div>
      <div>
        <Link to="/model/$modelId" params={{ modelId: model.id }}>
          {model.nameOrKey}
        </Link>
      </div>
      <div>
        <Text>{t("relationshipPage_tagsLabel")}</Text>
      </div>
      <div>
        <InlineEditTags
          value={relationship.tags}
          scope={modelTagScope(model.id)}
          onChange={handleChangeTags}
        >
          {relationship.tags.length === 0 ? (
            <MissingInformation>{t("relationshipPage_tagsEmpty")}</MissingInformation>
          ) : (
            <Tags tags={relationship.tags} scope={modelTagScope(model.id)} />
          )}
        </InlineEditTags>
      </div>
      {isDetailLevelTech && (
        <div>
          <Text>{t("relationshipPage_identifierLabel")}</Text>
        </div>
      )}
      {isDetailLevelTech && (
        <div>
          <Text>
            <code>{relationship.id}</code>
          </Text>
        </div>
      )}
    </PropertiesForm>
  );
}

function roleCardinalityLabel(
  c: RelationshipRoleDto["cardinality"],
  t: (key: AppMessageKey, values?: Record<string, unknown>) => string,
) {
  if (c === "zeroOrOne") return t("relationshipPage_cardinalityMaybeOne");
  if (c === "many") return t("relationshipPage_cardinalityMany");
  if (c === "one") return t("relationshipPage_cardinalityOne");
  if (c === "unknown") return t("relationshipPage_cardinalityUnknown");
}
