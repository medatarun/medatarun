import { Link, useNavigate } from "@tanstack/react-router";
import {
  createActionCtxModel,
  createActionCtxRelationship,
  createDisplayedSubjectModel,
  type ElementOrigin,
  Model,
  type RelationshipDto,
} from "@/business/model";
import {
  useModel,
  useModelAddTag,
  useModelDeleteTag,
  useModelUpdateDescription,
  useModelUpdateDocumentationHome,
  useModelUpdateKey,
  useModelUpdateName,
} from "@/components/business/model";
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
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionCards } from "@/components/layout/SectionCards.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import {
  EntityIcon,
  ModelIcon,
  RelationshipIcon,
  TypeIcon,
} from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import { TagIcon } from "@/components/business/tag/tag.icons.tsx";
import { createActionCtxTag, Tag } from "@/business/tag";
import { useSecurityContext } from "@/components/business/security";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { useActionRegistry } from "@/components/business/actions";

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
  const sec = useSecurityContext();
  const { t } = useAppI18n();

  const displayNameWithAuthority = model.nameOrKey;

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

  const actions = actionRegistry.findActionDescriptors([
    "model_release",
    "model_update_authority",
    "model_copy",
    "model_delete",
  ]);

  const handleChangeName = (value: string) => {
    return modelUpdateName.mutateAsync({ modelId: model.id, value: value });
  };

  const displayedSubject = createDisplayedSubjectModel(model.id);

  const actionCtxPage = createActionCtxModel(model, displayedSubject);
  const actionCtxRelationship = (r: RelationshipDto) =>
    createActionCtxRelationship(model, r, displayedSubject);
  const actionCtxTag = (tag: Tag) =>
    createActionCtxTag(modelTagScope(model.id), displayedSubject, { tag: tag });

  const updateDescriptionDisabled = !sec.canExecuteAction(
    "model_update_description",
  );
  const handleChangeDescription = (v: string) => {
    return modelUpdateDescription.mutateAsync({
      modelId: model.id,
      value: v,
    });
  };

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("modelEditPage_eyebrow"),
    titleIcon: <ModelIcon authority={model.authority} />,
    title: (
      <InlineEditSingleLine
        value={model.name ?? ""}
        onChange={handleChangeName}
      >
        {displayNameWithAuthority}{" "}
      </InlineEditSingleLine>
    ),
    actions: {
      label: t("modelEditPage_actions"),
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
        <ModelOverview />
      </SectionPaper>
      <SectionPaper topspacing="XXXL" nopadding>
        <InlineEditDescription
          value={model.description}
          placeholder={t("modelEditPage_descriptionPlaceholder")}
          disabled={updateDescriptionDisabled}
          onChange={handleChangeDescription}
        />
      </SectionPaper>

      <SectionTitle
        icon={<EntityIcon />}
        actionCtx={actionCtxPage}
        actions={["entity_create"]}
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
        actionCtx={actionCtxPage}
        actions={["relationship_create"]}
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
            actionCtxRelationship={actionCtxRelationship}
          />
        </SectionTable>
      )}

      <SectionTitle
        icon={<TypeIcon />}
        actionCtx={actionCtxPage}
        actions={["type_create"]}
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
        actionCtx={actionCtxPage}
        actions={["tag_local_create"]}
      >
        {t("modelEditPage_localTagsTitle")}
      </SectionTitle>

      <SectionTable>
        <TagsTable
          scope={modelTagScope(model.id)}
          actionCtxTag={actionCtxTag}
        />
      </SectionTable>

      <ViewLayoutTechnicalInfos
        technicalKey={model.key}
        keyLabel={t("modelEditPage_keyLabel")}
        id={model.id}
        idLabel={t("modelEditPage_identifierLabel")}
      ></ViewLayoutTechnicalInfos>
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
  const sec = useSecurityContext();

  const displayedSubject = createDisplayedSubjectModel(model.id);
  const authorityLabel =
    model.authority === "canonical"
      ? t("modelEditPage_authorityCanonical")
      : t("modelEditPage_authoritySystem");

  const updateKeyDisabled = !sec.canExecuteAction("model_update_key");
  const updateDocumentationHomeDisabled = !sec.canExecuteAction(
    "model_update_documentation_link",
  );
  const updateTagDisabled = !sec.canExecuteActions(
    "model_add_tag",
    "model_delete_tag",
  );

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
          <InlineEditSingleLine
            value={model.key}
            disabled={updateKeyDisabled}
            onChange={handleChangeKey}
          >
            <Text>{model.key}</Text>
          </InlineEditSingleLine>
        </div>
      )}

      <div>{t("modelEditPage_authorityLabel")}</div>
      <div>
        <ModelIcon authority={model.authority} /> {authorityLabel}
      </div>

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
          disabled={updateDocumentationHomeDisabled}
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
          disabled={updateTagDisabled}
          onChange={handleChangeTags}
          displayedSubject={displayedSubject}
        >
          {!model.hasTags ? (
            <MissingInformation>
              {t("modelEditPage_tagsEmpty")}
            </MissingInformation>
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
