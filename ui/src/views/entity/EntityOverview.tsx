import { Text } from "@fluentui/react-components";
import { ExternalUrl, Origin } from "../model/ModelEditPage.tsx";
import { Link } from "@tanstack/react-router";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import {
  type EntityDto,
  useEntityAddTag,
  useEntityDeleteTag,
  useEntityUpdateDocumentationHome,
  useEntityUpdateKey,
} from "@/business/model";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import { useModelContext } from "@/components/business/model/ModelContext.tsx";
import { InlineEditTags } from "@/components/core/InlineEditTags.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { createDisplayedSubjectEntity } from "@/components/business/model/model.actions.ts";

export function EntityOverview({ entity }: { entity: EntityDto }) {
  const model = useModelContext();
  const { isDetailLevelTech } = useDetailLevelContext();
  const entityUpdateKey = useEntityUpdateKey();
  const entityUpdateDocumentationHome = useEntityUpdateDocumentationHome();
  const entityAddTag = useEntityAddTag();
  const entityDeleteTag = useEntityDeleteTag();
  const { t } = useAppI18n();

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
        <div>
          <Text>{t("entityEditPage_keyLabel")}</Text>
        </div>
      )}
      {isDetailLevelTech && (
        <div>
          <InlineEditSingleLine value={entity.key} onChange={handleChangeKey}>
            <Text>
              <code>{entity.key}</code>
            </Text>
          </InlineEditSingleLine>
        </div>
      )}
      <div>
        <Text>{t("entityEditPage_fromModelLabel")}</Text>
      </div>
      <div>
        <Link to="/model/$modelId" params={{ modelId: model.id }}>
          {model.nameOrKey}
        </Link>
      </div>

      <div>
        <Text>{t("entityEditPage_externalLinkLabel")}</Text>
      </div>
      <div>
        <InlineEditSingleLine
          value={entity.documentationHome ?? ""}
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
      </div>

      <div>
        <Text>{t("entityEditPage_tagsLabel")}</Text>
      </div>
      <div>
        <InlineEditTags
          value={entity.tags}
          scope={modelTagScope(model.id)}
          onChange={handleChangeTags}
          displayedSubject={createDisplayedSubjectEntity(model.id, entity.id)}
        >
          {entity.tags.length === 0 ? (
            <MissingInformation>
              {t("entityEditPage_tagsEmpty")}
            </MissingInformation>
          ) : (
            <Tags tags={entity.tags} scope={modelTagScope(model.id)} />
          )}
        </InlineEditTags>
      </div>

      <div>
        <Text>{t("entityEditPage_originLabel")}</Text>
      </div>
      <div>
        <Origin value={entity.origin} />
      </div>
      {isDetailLevelTech && (
        <div>
          <Text>{t("entityEditPage_identifierLabel")}</Text>
        </div>
      )}
      {isDetailLevelTech && (
        <div>
          <Text>
            <code>{entity.id}</code>
          </Text>
        </div>
      )}
    </PropertiesForm>
  );
}
