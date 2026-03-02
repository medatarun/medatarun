import { Link, useNavigate } from "@tanstack/react-router";
import { useActionRegistry } from "@/business/action_registry";
import { Model, useModel } from "@/business/model";
import {
  type Tag,
  useTags,
  useTagFreeUpdateDescription,
  useTagFreeUpdateName,
  useTagManagedUpdateDescription,
  useTagManagedUpdateName,
} from "@/business/tag";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
  tokens,
} from "@fluentui/react-components";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  createActionTemplateTag,
  detailActionLocation,
} from "@/components/business/tag/tag.actions.ts";
import { TagGroupIcon, TagIcon } from "@/components/business/tag/tag.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";

export function TagEdit({ tagId }: { tagId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const tagManagedUpdateName = useTagManagedUpdateName();
  const tagManagedUpdateDescription = useTagManagedUpdateDescription();
  const tagFreeUpdateName = useTagFreeUpdateName();
  const tagFreeUpdateDescription = useTagFreeUpdateDescription();

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;

  const tag = tagsResult.tags.findTag(tagId);
  if (!tag)
    return <ErrorBox error={toProblem(t("tagEdit_notFound", { tagId }))} />;

  const isGlobalTag = tag.isGlobal;

  const actions = actionRegistry.findActions(detailActionLocation(tag));

  const handleClickTagGroups = () => {
    navigate({ to: "/tag-groups" });
  };

  const handleClickTagGroup = () => {
    if (!tag.groupId) {
      return;
    }
    navigate({
      to: "/tag-group/$tagGroupId",
      params: { tagGroupId: tag.groupId },
    });
  };

  const handleChangeName = (value: string) => {
    if (isGlobalTag) {
      return tagManagedUpdateName.mutateAsync({ tagId: tag.id, value: value });
    }
    return tagFreeUpdateName.mutateAsync({ tagId: tag.id, value: value });
  };

  const handleChangeDescription = (value: string) => {
    if (isGlobalTag) {
      return tagManagedUpdateDescription.mutateAsync({
        tagId: tag.id,
        value: value,
      });
    }
    return tagFreeUpdateDescription.mutateAsync({
      tagId: tag.id,
      value: value,
    });
  };

  return (
    <ViewLayoutContained
      title={
        <div>
          <div style={{ marginLeft: "-22px" }}>
            <Breadcrumb size="small">
              <TagEditBreadcrumb
                tag={tag}
                onClickTagGroups={handleClickTagGroups}
                onClickTagGroup={handleClickTagGroup}
              />
            </Breadcrumb>
          </div>
          <ViewTitle
            eyebrow={
              isGlobalTag
                ? t("tagEdit_globalEyebrow")
                : t("tagEdit_localEyebrow")
            }
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div style={{ width: "100%" }}>
                <InlineEditSingleLine
                  value={tag.name ?? ""}
                  onChange={handleChangeName}
                >
                  {tag.name ? (
                    tag.name
                  ) : (
                    <MissingInformation>{tag.key}</MissingInformation>
                  )}{" "}
                </InlineEditSingleLine>
              </div>
              <div>
                <ActionMenuButton
                  label={t("tagEdit_actions")}
                  itemActions={actions}
                  actionParams={createActionTemplateTag(tag.id)}
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
              <TagOverview tag={tag} />
            </SectionPaper>
            <SectionPaper topspacing="XXXL" nopadding>
              <InlineEditDescription
                value={tag.description}
                placeholder={t("tagEdit_descriptionPlaceholder")}
                onChange={handleChangeDescription}
              />
            </SectionPaper>
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}

function TagEditBreadcrumb({
  tag,
  onClickTagGroups,
  onClickTagGroup,
}: {
  tag: Tag;
  onClickTagGroups: () => void;
  onClickTagGroup: () => void;
}) {
  const { t } = useAppI18n();
  const modelId = tag.scope.type === "model" ? tag.scope.id : null;

  if (tag.isGlobal) {
    return (
      <>
        <BreadcrumbItem>
          <BreadcrumbButton icon={<TagGroupIcon />} onClick={onClickTagGroups}>
            {t("tagEdit_breadcrumbTagGroups")}
          </BreadcrumbButton>
        </BreadcrumbItem>
        <BreadcrumbDivider />
        {tag.groupLabel && (
          <>
            <BreadcrumbItem>
              <BreadcrumbButton icon={<TagIcon />} onClick={onClickTagGroup}>
                {tag.groupLabel}
              </BreadcrumbButton>
            </BreadcrumbItem>
            <BreadcrumbDivider />
          </>
        )}
      </>
    );
  }

  if (modelId) {
    return <LocalModelBreadcrumb modelId={modelId} />;
  }

  return (
    <>
      <BreadcrumbItem>
        <Text>{tag.scope.type}</Text>
      </BreadcrumbItem>
      <BreadcrumbDivider />
    </>
  );
}

function LocalModelBreadcrumb({ modelId }: { modelId: string }) {
  const { t } = useAppI18n();
  const { data: modelDto } = useModel(modelId);
  const model = modelDto ? new Model(modelDto) : null;

  if (!model) {
    return (
      <>
        <BreadcrumbItem>
          <Text>{t("tagEdit_localModelFallback")}</Text>
        </BreadcrumbItem>
        <BreadcrumbDivider />
      </>
    );
  }

  return (
    <>
      <BreadcrumbItem>
        <Link to="/model/$modelId" params={{ modelId: modelId }}>
          {model.nameOrKey}
        </Link>
      </BreadcrumbItem>
      <BreadcrumbDivider />
    </>
  );
}

function TagOverview({ tag }: { tag: Tag }) {
  const { t } = useAppI18n();
  return (
    <PropertiesForm>
      <div>
        <Text>{t("tagEdit_scopeLabel")}</Text>
      </div>
      <div>
        <Text>{tag.scopeLabel}</Text>
      </div>

      <div>
        <Text>{t("tagEdit_keyLabel")}</Text>
      </div>
      <div>
        <Text>
          <code>{tag.key}</code>
        </Text>
      </div>

      <div>
        <Text>{t("tagEdit_identifierLabel")}</Text>
      </div>
      <div>
        <Text>
          <code>{tag.id}</code>
        </Text>
      </div>
    </PropertiesForm>
  );
}
