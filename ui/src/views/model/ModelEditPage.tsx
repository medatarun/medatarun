import { Link, useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  type ElementOrigin,
  Model,
  useModel,
  useModelAddTag,
  useModelDeleteTag,
  useModelUpdateDescription,
  useModelUpdateDocumentationHome,
  useModelUpdateKey,
  useModelUpdateName,
} from "@/business/model";
import {
  ModelContext,
  useModelContext,
} from "@/components/business/model/ModelContext.tsx";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import { InfoLabel, Text, tokens } from "@fluentui/react-components";
import { HistoryRegular } from "@fluentui/react-icons";
import { EntityCard } from "@/components/business/model/EntityCard.tsx";
import { RelationshipsTable } from "@/components/business/model/RelationshipsTable.tsx";
import { TypesTable } from "@/components/business/model/TypesTable.tsx";
import { TagsTable } from "@/components/business/tag/TagsTable.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionCards } from "@/components/layout/SectionCards.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import {
  createActionTemplateModel,
  createDisplayedSubjectModel,
} from "@/components/business/model/model.actions.ts";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import { createActionTemplateTagLocalList } from "@/components/business/tag/tag.actions.ts";
import {
  EntityIcon,
  RelationshipIcon,
  TypeIcon,
} from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import { TagIcon } from "@/components/business/tag/tag.icons.tsx";

export function ModelEditPage({ modelId }: { modelId: string }) {
  const { data: model } = useModel(modelId);

  return (
    <div>
      {model && (
        <ModelContext value={new Model(model)}>
          <ModelView />
        </ModelContext>
      )}
    </div>
  );
}

export function ModelView() {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const navigate = useNavigate();
  const modelUpdateDescription = useModelUpdateDescription();
  const modelUpdateName = useModelUpdateName();
  const { t } = useAppI18n();

  const displayNameWithAuthority = model.nameOrKeyWithAuthorityEmoji;

  const handleClickType = (typeId: string) => {
    navigate({
      to: "/model/$modelId/type/$typeId",
      params: { modelId: model.id, typeId: typeId },
    });
  };
  const handleClickRelationship = (relationshipId: string) => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: { modelId: model.id, relationshipId: relationshipId },
    });
  };
  const handleClickEntity = (entityId: string) => {
    navigate({
      to: "/model/$modelId/entity/$entityId",
      params: { modelId: model.id, entityId: entityId },
    });
  };

  const actions = actionRegistry.findActions(ActionUILocations.model_overview);

  const handleChangeName = (value: string) => {
    return modelUpdateName.mutateAsync({ modelId: model.id, value: value });
  };

  const displayedSubject = createDisplayedSubjectModel(model.id);
  return (
    <ViewLayoutContained
      title={
        <div>
          <ViewTitle eyebrow={<span>{t("modelEditPage_eyebrow")}</span>}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div style={{ width: "100%" }}>
                <InlineEditSingleLine
                  value={model.name ?? ""}
                  onChange={handleChangeName}
                >
                  {displayNameWithAuthority}{" "}
                </InlineEditSingleLine>
              </div>
              <div>
                <ActionMenuButton
                  label={t("modelEditPage_actions")}
                  itemActions={actions}
                  actionParams={createActionTemplateModel(model.id)}
                  displayedSubject={displayedSubject}
                />
              </div>
            </div>
          </ViewTitle>
        </div>
      }
    >
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <ContainedHumanReadable>
            <SectionPaper>
              <ModelOverview />
            </SectionPaper>
            <SectionPaper topspacing="XXXL" nopadding>
              <InlineEditDescription
                value={model.description}
                placeholder={t("modelEditPage_descriptionPlaceholder")}
                onChange={(v) =>
                  modelUpdateDescription.mutateAsync({
                    modelId: model.id,
                    value: v,
                  })
                }
              />
            </SectionPaper>

            <SectionTitle
              icon={<EntityIcon />}
              actionParams={createActionTemplateModel(model.id)}
              displayedSubject={displayedSubject}
              location={ActionUILocations.model_entities}
            >
              {t("modelEditPage_entitiesTitle")}
            </SectionTitle>

            {!model.hasEntities && (
              <p>
                <MissingInformation>
                  {t("modelEditPage_entitiesEmpty")}
                </MissingInformation>
              </p>
            )}
            {model.hasEntities && (
              <SectionCards>
                <EntitiesCardList onClick={handleClickEntity} />
              </SectionCards>
            )}

            <SectionTitle
              icon={<RelationshipIcon />}
              actionParams={createActionTemplateModel(model.id)}
              displayedSubject={displayedSubject}
              location={ActionUILocations.model_relationships}
            >
              {t("modelEditPage_relationshipsTitle")}
            </SectionTitle>

            {!model.hasRelationships && (
              <p>
                <MissingInformation>
                  {t("modelEditPage_relationshipsEmpty")}
                </MissingInformation>
              </p>
            )}
            {model.hasRelationships && (
              <SectionTable>
                <RelationshipsTable
                  onClick={handleClickRelationship}
                  relationships={model.relationships}
                  displayedSubject={displayedSubject}
                />
              </SectionTable>
            )}

            <SectionTitle
              icon={<TypeIcon />}
              actionParams={createActionTemplateModel(model.id)}
              displayedSubject={displayedSubject}
              location={ActionUILocations.model_types}
            >
              {t("modelEditPage_dataTypesTitle")}
            </SectionTitle>

            {!model.hasTypes && (
              <p>
                <MissingInformation>
                  {t("modelEditPage_dataTypesEmpty")}
                </MissingInformation>
              </p>
            )}
            {model.hasTypes && (
              <SectionCards>
                <TypesTable onClick={handleClickType} types={model.types} />
              </SectionCards>
            )}

            <SectionTitle
              icon={<TagIcon />}
              actionParams={createActionTemplateTagLocalList(
                modelTagScope(model.id),
              )}
              location={ActionUILocations.tag_local_list}
              displayedSubject={displayedSubject}
            >
              {t("modelEditPage_localTagsTitle")}
            </SectionTitle>

            <SectionTable>
              <TagsTable
                scope={modelTagScope(model.id)}
                displayedSubject={displayedSubject}
              />
            </SectionTable>

            <ViewLayoutTechnicalInfos
              technicalKey={model.key}
              keyLabel={t("modelEditPage_keyLabel")}
              id={model.id}
              idLabel={t("modelEditPage_identifierLabel")}
            ></ViewLayoutTechnicalInfos>
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}

export function ModelOverview() {
  const model = useModelContext();
  const { isDetailLevelTech } = useDetailLevelContext();
  const modelUpdateKey = useModelUpdateKey();
  const modelUpdateDocumentationHome = useModelUpdateDocumentationHome();
  const modelUpdateAddTag = useModelAddTag();
  const modelUpdateDeleteTag = useModelDeleteTag();
  const { t } = useAppI18n();
  const displayedSubject = createDisplayedSubjectModel(model.id);
  const authorityLabel =
    model.authority === "canonical"
      ? t("modelEditPage_authorityCanonical")
      : t("modelEditPage_authoritySystem");

  const handleChangeKey = (value: string) => {
    return modelUpdateKey.mutateAsync({ modelId: model.id, value: value });
  };
  const handleChangeDocumentationHome = (value: string) => {
    return modelUpdateDocumentationHome.mutateAsync({
      modelId: model.id,
      value: value,
    });
  };
  const handleChangeTags = async (value: string[]) => {
    for (const tag of model.findTagsToDelete(value)) {
      await modelUpdateDeleteTag.mutateAsync({
        modelId: model.id,
        tag: "id:" + tag,
      });
    }
    for (const tag of model.findTagsToAdd(value)) {
      await modelUpdateAddTag.mutateAsync({
        modelId: model.id,
        tag: "id:" + tag,
      });
    }
  };
  return (
    <PropertiesForm>
      {isDetailLevelTech && (
        <div>
          <InfoLabel>{t("modelEditPage_keyLabel")}</InfoLabel>
        </div>
      )}
      {isDetailLevelTech && (
        <div>
          <InlineEditSingleLine value={model.key} onChange={handleChangeKey}>
            <Text>
              <code>{model.key}</code>
            </Text>
          </InlineEditSingleLine>
        </div>
      )}

      <div>{t("modelEditPage_authorityLabel")}</div>
      <div>{`${model.authorityEmoji} ${authorityLabel}`}</div>

      <div>{t("modelEditPage_versionLabel")}</div>
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: tokens.spacingHorizontalM,
        }}
      >
        <span>{model.version}</span>
        <Link
          to="/model/$modelId/history"
          params={{ modelId: model.id }}
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: tokens.spacingHorizontalXS,
          }}
        >
          <HistoryRegular />
          <span>{t("modelEditPage_historyLink")}</span>
        </Link>
      </div>

      <div>{t("modelEditPage_externalLinkLabel")}</div>
      <div>
        <InlineEditSingleLine
          value={model.documentationHome ?? ""}
          onChange={handleChangeDocumentationHome}
        >
          {!model.documentationHome ? (
            <MissingInformation>
              {t("modelEditPage_externalLinkEmpty")}
            </MissingInformation>
          ) : (
            <ExternalUrl url={model.documentationHome} />
          )}
        </InlineEditSingleLine>
      </div>

      <div>{t("modelEditPage_tagsLabel")}</div>
      <div>
        <InlineEditTags
          value={model.tags}
          scope={modelTagScope(model.id)}
          onChange={handleChangeTags}
          displayedSubject={displayedSubject}
        >
          {!model.hasTags ? (
            <MissingInformation>{t("modelEditPage_tagsEmpty")}</MissingInformation>
          ) : (
            <Tags tags={model.tags} scope={modelTagScope(model.id)} />
          )}
        </InlineEditTags>
      </div>
      {isDetailLevelTech && <div>{t("modelEditPage_originLabel")}</div>}
      {isDetailLevelTech && (
        <div>
          <Origin value={model.origin} />
        </div>
      )}
    </PropertiesForm>
  );
}

export function EntitiesCardList({
  onClick,
}: {
  onClick: (entityId: string) => void;
}) {
  const model = useModelContext();
  const entities = model.entities;
  return (
    <div>
      <div
        style={{
          display: "grid",
          columnGap: tokens.spacingHorizontalM,
          rowGap: tokens.spacingVerticalM,
          paddingTop: tokens.spacingVerticalM,
          justifyContent: "space-evenly",
          flexWrap: "wrap",
          gridTemplateColumns: "repeat(3, 1fr)",
        }}
      >
        {entities.map((entity) => (
          <EntityCard
            key={entity.id}
            entity={entity}
            onClick={() => {
              onClick(entity.id);
            }}
          />
        ))}
      </div>
    </div>
  );
}

export function Origin({ value }: { value: ElementOrigin }) {
  const { t } = useAppI18n();
  if (value.type == "manual") return t("modelEditPage_manualOrigin");
  return <ExternalUrl url={value.uri} />;
}

export function ExternalUrl({ url }: { url: string | null }) {
  if (!url) return null;
  return (
    <a href={url} target="_blank">
      {url}
    </a>
  );
}
