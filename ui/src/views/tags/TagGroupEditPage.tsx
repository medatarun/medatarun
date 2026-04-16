import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  Tag,
  useTagGroupUpdateDescription,
  useTagGroupUpdateName,
  useTags,
} from "@/business/tag";
import { TagsTable } from "@/components/business/tag/TagsTable.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
} from "@fluentui/react-components";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  createActionTemplateTag,
  createActionTemplateTagGlobalList,
  createActionTemplateTagGroup,
  createDisplayedSubjectTagGroup,
} from "@/components/business/tag/tag.actions.ts";
import { TagGroupIcon } from "@/components/business/tag/tag.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import type { ActionCtx } from "@/components/business/actions";

export function TagGroupEditPage({ tagGroupId }: { tagGroupId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const tagGroupUpdateName = useTagGroupUpdateName();
  const tagGroupUpdateDescription = useTagGroupUpdateDescription();

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;

  const tagGroup = tagsResult.tags.findTagGroup(tagGroupId);
  if (!tagGroup)
    return (
      <ErrorBox error={toProblem(t("tagGroupEdit_notFound", { tagGroupId }))} />
    );

  const actions = actionRegistry.findActions(
    ActionUILocations.tag_group_detail,
  );

  const handleClickTagGroups = () => {
    navigate({ to: "/tag-groups" });
  };

  const handleChangeName = (value: string) => {
    return tagGroupUpdateName.mutateAsync({
      tagGroupId: tagGroup.id,
      value: value,
    });
  };

  const handleChangeDescription = (value: string) => {
    return tagGroupUpdateDescription.mutateAsync({
      tagGroupId: tagGroup.id,
      value: value,
    });
  };

  const displayedSubject = createDisplayedSubjectTagGroup(tagGroup.id);
  const actionCtxPage: ActionCtx = {
    actionParams: createActionTemplateTagGroup(tagGroup.id),
    displayedSubject: displayedSubject,
  };
  const actionCtxTagList: ActionCtx = {
    actionParams: createActionTemplateTagGlobalList(tagGroup.id),
    displayedSubject: displayedSubject,
  };
  const actionCtxTag = (tag: Tag): ActionCtx => ({
    actionParams: createActionTemplateTag(tag.id),
    displayedSubject: displayedSubject,
  });

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<TagGroupIcon />}
          onClick={handleClickTagGroups}
        >
          {t("tagGroupEdit_breadcrumb")}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton icon={<TagGroupIcon />} current={true}>
          {t("tagGroupEdit_eyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );
  const headerProps: ViewLayoutHeaderProps = {
    title: (
      <InlineEditSingleLine
        value={tagGroup.name ?? ""}
        onChange={handleChangeName}
      >
        {tagGroup.name ? (
          tagGroup.name
        ) : (
          <MissingInformation>{tagGroup.key}</MissingInformation>
        )}{" "}
      </InlineEditSingleLine>
    ),
    titleIcon: <TagGroupIcon />,
    breadcrumb: breadcrumb,
    actions: {
      label: t("tagGroupEdit_actions"),
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
          value={tagGroup.description}
          placeholder={t("tagGroupEdit_descriptionPlaceholder")}
          onChange={handleChangeDescription}
        />
      </SectionPaper>

      <SectionTitle
        icon={<TagGroupIcon />}
        location={ActionUILocations.tag_global_list}
        actionCtx={actionCtxTagList}
      >
        {t("tagGroupEdit_tagsTitle")}
      </SectionTitle>

      <SectionTable>
        <TagsTable
          scope={{ type: "global", id: null }}
          tagGroupId={tagGroup.id}
          actionCtxTag={actionCtxTag}
        />
      </SectionTable>
      <ViewLayoutTechnicalInfos
        id={tagGroup.id}
        idLabel={t("tagGroupEdit_identifierLabel")}
        technicalKey={tagGroup.key}
        keyLabel={t("tagGroupEdit_groupKeyLabel")}
      />
    </ViewLayoutContained>
  );
}
