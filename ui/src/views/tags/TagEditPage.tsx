import { useNavigate } from "@tanstack/react-router";
import { useActionRegistry } from "@/business/action_registry";
import { Model, useModel } from "@/business/model";
import {
  type Tag,
  useTagGlobalUpdateDescription,
  useTagGlobalUpdateName,
  useTagLocalUpdateDescription,
  useTagLocalUpdateName,
  useTags,
} from "@/business/tag";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
} from "@fluentui/react-components";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  createActionTemplateTag,
  createDisplayedSubjectTag,
  detailActionLocation,
} from "@/components/business/tag/tag.actions.ts";
import { TagGroupIcon, TagIcon } from "@/components/business/tag/tag.icons.tsx";
import { ModelIcon } from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import type { ActionCtx } from "@/components/business/actions";

export function TagEditPage({ tagId }: { tagId: string }) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const tagGlobalUpdateName = useTagGlobalUpdateName();
  const tagGlobalUpdateDescription = useTagGlobalUpdateDescription();
  const tagLocalUpdateName = useTagLocalUpdateName();
  const tagLocalUpdateDescription = useTagLocalUpdateDescription();

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;

  const tag = tagsResult.tags.findTag(tagId);
  if (!tag)
    return <ErrorBox error={toProblem(t("tagEdit_notFound", { tagId }))} />;

  const isGlobalTag = tag.isGlobal;

  const actions = actionRegistry.findActions(detailActionLocation(tag));

  const handleChangeName = (value: string) => {
    if (isGlobalTag) {
      return tagGlobalUpdateName.mutateAsync({ tagId: tag.id, value: value });
    }
    return tagLocalUpdateName.mutateAsync({ tagId: tag.id, value: value });
  };

  const handleChangeDescription = (value: string) => {
    if (isGlobalTag) {
      return tagGlobalUpdateDescription.mutateAsync({
        tagId: tag.id,
        value: value,
      });
    }
    return tagLocalUpdateDescription.mutateAsync({
      tagId: tag.id,
      value: value,
    });
  };

  const actionCtxPage: ActionCtx = {
    actionParams: createActionTemplateTag(tag.id),
    displayedSubject: createDisplayedSubjectTag({
      tagId: tag.id,
      tagScopeRef: tag.scope,
      tagGroupId: tag.groupId,
    }),
  };

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: <TagEditBreadcrumb tag={tag} />,
    title: (
      <InlineEditSingleLine value={tag.name ?? ""} onChange={handleChangeName}>
        {tag.name ? (
          tag.name
        ) : (
          <MissingInformation>{tag.key}</MissingInformation>
        )}{" "}
      </InlineEditSingleLine>
    ),
    titleIcon: <TagIcon />,
    actions: {
      label: t("tagEdit_actions"),
      itemActions: actions,
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <SectionPaper topspacing="XXXL" nopadding>
        <InlineEditDescription
          value={tag.description}
          placeholder={t("tagEdit_descriptionPlaceholder")}
          onChange={handleChangeDescription}
        />
      </SectionPaper>
      <ViewLayoutTechnicalInfos
        technicalKey={tag.key}
        keyLabel={t("tagEdit_keyLabel")}
        id={tag.id}
        idLabel={t("tagEdit_identifierLabel")}
      />
    </ViewLayoutContained>
  );
}

function TagEditBreadcrumb({ tag }: { tag: Tag }) {
  const modelId = tag.scope.type === "model" ? tag.scope.id : null;

  if (tag.isGlobal) {
    return <GlobalBreadcrumb tag={tag} />;
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

function GlobalBreadcrumb({ tag }: { tag: Tag }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
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

  return (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<TagGroupIcon />}
          onClick={handleClickTagGroups}
        >
          {t("tagEdit_breadcrumbTagGroups")}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton icon={<TagIcon />} onClick={handleClickTagGroup}>
          {tag.groupLabel}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton current={true}>
          {t("tagEdit_globalEyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );
}

function LocalModelBreadcrumb({ modelId }: { modelId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const { data: modelDto } = useModel(modelId);
  const model = modelDto ? new Model(modelDto) : null;

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: { modelId: modelId },
    });
  };

  if (!model) {
    return (
      <Breadcrumb size="small">
        <BreadcrumbItem>
          <BreadcrumbButton>
            <Text>{t("tagEdit_localModelFallback")}</Text>
          </BreadcrumbButton>
        </BreadcrumbItem>
        <BreadcrumbDivider />
      </Breadcrumb>
    );
  }

  return (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton icon={<ModelIcon />} onClick={handleClickModel}>
          {model.nameOrKeyWithAuthorityEmoji}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton onClick={handleClickModel}>
          {t("tagEdit_localEyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );
}
