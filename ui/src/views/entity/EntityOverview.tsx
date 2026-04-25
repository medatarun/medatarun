import { Text } from "@fluentui/react-components";
import { ExternalUrl, Origin } from "../model/ModelEditPage.tsx";
import { Link } from "@tanstack/react-router";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import { type EntityDto } from "@/business/model";
import {
  useEntityAddTag,
  useEntityDeleteTag,
  useEntityUpdateDocumentationHome,
  useEntityUpdateKey,
} from "@/components/business/model";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import {
  PropertiesForm,
  PropertyLabel,
  PropertyValue,
} from "@/components/layout/PropertiesForm.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { useModelContext } from "@/components/business/model/ModelContext.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import type { ActionDisplayedSubject } from "@/business/action-performer";
import { useSecurityContext } from "@/components/business/security";

export function EntityOverview({
  entity,
  displayedSubject,
}: {
  entity: EntityDto;
  displayedSubject: ActionDisplayedSubject;
}) {
  const model = useModelContext();
  const { isDetailLevelTech } = useDetailLevelContext();
  const entityUpdateKey = useEntityUpdateKey();
  const entityUpdateDocumentationHome = useEntityUpdateDocumentationHome();
  const entityAddTag = useEntityAddTag();
  const entityDeleteTag = useEntityDeleteTag();
  const securityContext = useSecurityContext();
  const { t } = useAppI18n();

  const changeKeyDisabled =
    !securityContext.canExecuteAction("entity_update_key");
  const changeDocumentationHomeDisabled = !securityContext.canExecuteAction(
    "entity_update_documentation_link",
  );
  const changeTagsDisabled = !securityContext.canExecuteActions(
    "entity_add_tag",
    "entity_delete_tag",
  );

  const handleChangeKey = (value: string) => {
    return entityUpdateKey.mutateAsync({
      modelId: model.id,
      entityId: entity.id,
      value: value,
    });
  };
  const handleChangeDocumentationHome = (value: string) => {
    return entityUpdateDocumentationHome.mutateAsync({
      modelId: model.id,
      entityId: entity.id,
      value: value,
    });
  };
  const handleChangeTags = async (value: string[]) => {
    for (const tag of entity.tags) {
      if (!value.includes(tag))
        await entityDeleteTag.mutateAsync({
          modelId: model.id,
          entityId: entity.id,
          tag: "id:" + tag,
        });
    }
    for (const tag of value) {
      if (!entity.tags.includes(tag))
        await entityAddTag.mutateAsync({
          modelId: model.id,
          entityId: entity.id,
          tag: "id:" + tag,
        });
    }
  };
  return (
    <PropertiesForm>
      {isDetailLevelTech && (
        <PropertyLabel>{t("entityEditPage_keyLabel")}</PropertyLabel>
      )}
      {isDetailLevelTech && (
        <PropertyValue>
          <InlineEditSingleLine
            value={entity.key}
            disabled={changeKeyDisabled}
            onChange={handleChangeKey}
          >
            <code>{entity.key}</code>
          </InlineEditSingleLine>
        </PropertyValue>
      )}
      <PropertyLabel>{t("entityEditPage_fromModelLabel")}</PropertyLabel>
      <PropertyValue>
        <Link to="/model/$modelId" params={{ modelId: model.id }}>
          {model.nameOrKey}
        </Link>
      </PropertyValue>

      <PropertyLabel>{t("entityEditPage_externalLinkLabel")}</PropertyLabel>
      <PropertyValue>
        <InlineEditSingleLine
          value={entity.documentationHome ?? ""}
          disabled={changeDocumentationHomeDisabled}
          onChange={handleChangeDocumentationHome}
        >
          {!entity.documentationHome ? (
            <MissingInformation>
              {t("entityEditPage_externalLinkEmpty")}
            </MissingInformation>
          ) : (
            <ExternalUrl url={entity.documentationHome} />
          )}
        </InlineEditSingleLine>
      </PropertyValue>

      <PropertyLabel>{t("entityEditPage_tagsLabel")}</PropertyLabel>
      <PropertyValue>
        <InlineEditTags
          value={entity.tags}
          scope={modelTagScope(model.id)}
          disabled={changeTagsDisabled}
          onChange={handleChangeTags}
          displayedSubject={displayedSubject}
        >
          {entity.tags.length === 0 ? (
            <MissingInformation>
              {t("entityEditPage_tagsEmpty")}
            </MissingInformation>
          ) : (
            <Tags tags={entity.tags} scope={modelTagScope(model.id)} />
          )}
        </InlineEditTags>
      </PropertyValue>

      <PropertyLabel>{t("entityEditPage_originLabel")}</PropertyLabel>
      <PropertyValue>
        <Origin value={entity.origin} />
      </PropertyValue>
      {isDetailLevelTech && (
        <PropertyLabel>
          <Text>{t("entityEditPage_identifierLabel")}</Text>
        </PropertyLabel>
      )}
      {isDetailLevelTech && (
        <PropertyValue>
          <Text>
            <code>{entity.id}</code>
          </Text>
        </PropertyValue>
      )}
    </PropertiesForm>
  );
}
