import { useNavigate } from "@tanstack/react-router";
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
  useModelUpdateVersion,
} from "@/business/model";
import {
  ModelContext,
  useModelContext,
} from "@/components/business/model/ModelContext.tsx";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import { InfoLabel, Text, tokens } from "@fluentui/react-components";
import { EntityCard } from "@/components/business/model/EntityCard.tsx";
import { RelationshipsTable } from "@/components/business/model/RelationshipsTable.tsx";
import {
  ActionMenuButton,
  TypesTable,
} from "@/components/business/model/TypesTable.tsx";
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
import { createActionTemplateModel } from "@/components/business/model/model.actions.ts";
import { createDisplayedSubjectModel } from "@/components/business/model/model.actions.ts";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import { createActionTemplateTagFreeList } from "@/components/business/tag/tag.actions.ts";
import {
  EntityIcon,
  RelationshipIcon,
  TypeIcon,
} from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";

export function ModelPage({ modelId }: { modelId: string }) {
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
  const modelDto = model.dto
  const actionRegistry = useActionRegistry();
  const navigate = useNavigate();
  const modelUpdateDescription = useModelUpdateDescription();
  const modelUpdateName = useModelUpdateName();
  const { t } = useAppI18n();

  const displayNameWithAuthority = model.nameOrKeyWithAuthorityEmoji

  const handleClickType = (typeId: string) => {
    navigate({
      to: "/model/$modelId/type/$typeId",
      params: { modelId: modelDto.id, typeId: typeId },
    });
  };
  const handleClickRelationship = (relationshipId: string) => {
    navigate({
      to: "/model/$modelId/relationship/$relationshipId",
      params: { modelId: modelDto.id, relationshipId: relationshipId },
    });
  };
  const handleClickEntity = (entityId: string) => {
    navigate({
      to: "/model/$modelId/entity/$entityId",
      params: { modelId: modelDto.id, entityId: entityId },
    });
  };

  const actions = actionRegistry.findActions(ActionUILocations.model_overview);

  const handleChangeName = (value: string) => {
    return modelUpdateName.mutateAsync({ modelId: modelDto.id, value: value });
  };

  const displayedSubject = createDisplayedSubjectModel(modelDto.id);
  return (
    <ViewLayoutContained
      title={
        <div>
          <ViewTitle eyebrow={<span>{t("modelPage_eyebrow")}</span>}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div style={{ width: "100%" }}>
                <InlineEditSingleLine
                  value={modelDto.name ?? ""}
                  onChange={handleChangeName}
                >
                  {displayNameWithAuthority}{" "}
                </InlineEditSingleLine>
              </div>
              <div>
                <ActionMenuButton
                  label={t("modelPage_actions")}
                  itemActions={actions}
                  actionParams={createActionTemplateModel(modelDto.id)}
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
                value={modelDto.description}
                placeholder={t("modelPage_descriptionPlaceholder")}
                onChange={(v) =>
                  modelUpdateDescription.mutateAsync({
                    modelId: modelDto.id,
                    value: v,
                  })
                }
              />
            </SectionPaper>

            <SectionTitle
              icon={<EntityIcon />}
              actionParams={createActionTemplateModel(modelDto.id)}
              displayedSubject={displayedSubject}
              location={ActionUILocations.model_entities}
            >
              {t("modelPage_entitiesTitle")}
            </SectionTitle>

            {modelDto.entities.length === 0 && (
              <p>
                <MissingInformation>
                  {t("modelPage_entitiesEmpty")}
                </MissingInformation>
              </p>
            )}
            {modelDto.entities.length > 0 && (
              <SectionCards>
                <EntitiesCardList onClick={handleClickEntity} />
              </SectionCards>
            )}

            <SectionTitle
              icon={<RelationshipIcon />}
              actionParams={createActionTemplateModel(modelDto.id)}
              displayedSubject={displayedSubject}
              location={ActionUILocations.model_relationships}
            >
              {t("modelPage_relationshipsTitle")}
            </SectionTitle>

            {modelDto.relationships.length === 0 && (
              <p>
                <MissingInformation>
                  {t("modelPage_relationshipsEmpty")}
                </MissingInformation>
              </p>
            )}
            {modelDto.relationships.length > 0 && (
              <SectionTable>
                <RelationshipsTable
                  onClick={handleClickRelationship}
                  relationships={modelDto.relationships}
                  displayedSubject={displayedSubject}
                />
              </SectionTable>
            )}

            <SectionTitle
              icon={<TypeIcon />}
              actionParams={createActionTemplateModel(modelDto.id)}
              displayedSubject={displayedSubject}
              location={ActionUILocations.model_types}
            >
              {t("modelPage_dataTypesTitle")}
            </SectionTitle>

            {modelDto.types.length === 0 && (
              <p>
                <MissingInformation>
                  {t("modelPage_dataTypesEmpty")}
                </MissingInformation>
              </p>
            )}
            {modelDto.types.length > 0 && (
              <SectionTable>
                <TypesTable onClick={handleClickType} types={modelDto.types} />
              </SectionTable>
            )}

            <SectionTitle
              icon={<TypeIcon />}
              actionParams={createActionTemplateTagFreeList(
                modelTagScope(modelDto.id),
              )}
              location={ActionUILocations.tag_free_list}
              displayedSubject={displayedSubject}
            >
              {t("modelPage_localTagsTitle")}
            </SectionTitle>

            <SectionTable>
              <TagsTable
                scope={modelTagScope(modelDto.id)}
                displayedSubject={displayedSubject}
              />
            </SectionTable>
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}

export function ModelOverview() {
  const model = useModelContext().dto;
  const { isDetailLevelTech } = useDetailLevelContext();
  const modelUpdateVersion = useModelUpdateVersion();
  const modelUpdateKey = useModelUpdateKey();
  const modelUpdateDocumentationHome = useModelUpdateDocumentationHome();
  const modelUpdateAddTag = useModelAddTag();
  const modelUpdateDeleteTag = useModelDeleteTag();
  const { t } = useAppI18n();
  const displayedSubject = createDisplayedSubjectModel(model.id);

  const handleChangeVersion = (value: string) => {
    return modelUpdateVersion.mutateAsync({ modelId: model.id, value: value });
  };
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
    for (const tag of model.tags) {
      if (!value.includes(tag))
        await modelUpdateDeleteTag.mutateAsync({
          modelId: model.id,
          tag: "id:" + tag,
        });
    }
    for (const tag of value) {
      if (!model.tags.includes(tag))
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
          <InfoLabel>{t("modelPage_keyLabel")}</InfoLabel>
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

      <div>{t("modelPage_versionLabel")}</div>
      <div>
        <InlineEditSingleLine
          value={model.version}
          onChange={handleChangeVersion}
        >
          <code>{model.version}</code>
        </InlineEditSingleLine>
      </div>

      <div>{t("modelPage_externalLinkLabel")}</div>
      <div>
        <InlineEditSingleLine
          value={model.documentationHome ?? ""}
          onChange={handleChangeDocumentationHome}
        >
          {!model.documentationHome ? (
            <MissingInformation>
              {t("modelPage_externalLinkEmpty")}
            </MissingInformation>
          ) : (
            <ExternalUrl url={model.documentationHome} />
          )}
        </InlineEditSingleLine>
      </div>

      <div>{t("modelPage_tagsLabel")}</div>
      <div>
        <InlineEditTags
          value={model.tags}
          scope={modelTagScope(model.id)}
          onChange={handleChangeTags}
          displayedSubject={displayedSubject}
        >
          {model.tags.length === 0 ? (
            <MissingInformation>{t("modelPage_tagsEmpty")}</MissingInformation>
          ) : (
            <Tags tags={model.tags} scope={modelTagScope(model.id)} />
          )}
        </InlineEditTags>
      </div>
      {isDetailLevelTech && <div>{t("modelPage_originLabel")}</div>}
      {isDetailLevelTech && (
        <div>
          <Origin value={model.origin} />
        </div>
      )}
      {isDetailLevelTech && <div>{t("modelPage_identifierLabel")}</div>}
      {isDetailLevelTech && (
        <div>
          <code>{model.id}</code>
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
  const entities = model.dto.entities;
  return (
    <div>
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
  if (value.type == "manual") return t("modelPage_manualOrigin");
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
