import { useNavigate } from "@tanstack/react-router";
import {
  createActionCtxTag,
  createActionCtxTagGroup,
  createDisplayedSubjectTagGroup,
  Tag,
} from "@medatarun/ui/business/tag";
import { TagsTable } from "@medatarun/ui/components/business/tag/TagsTable.tsx";
import { MissingInformation } from "@medatarun/ui/components/core/MissingInformation.tsx";
import { InlineEditDescription } from "@medatarun/ui/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@medatarun/ui/components/core/InlineEditSingleLine.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
} from "@fluentui/react-components";
import { SectionTable } from "@medatarun/ui/components/layout/SecionTable.tsx";
import { SectionTitle } from "@medatarun/ui/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { TagGroupIcon } from "@medatarun/ui/components/business/tag/tag.icons.tsx";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutTechnicalInfos } from "@medatarun/ui/components/layout/ViewLayoutTechnicalInfos.tsx";
import { type ActionCtx } from "@medatarun/ui/business/action-performer";
import { useSecurityContext } from "@medatarun/ui/components/business/security";
import { useActionRegistry } from "@medatarun/ui/components/business/actions";
import {
  useTagGroupUpdateDescription,
  useTagGroupUpdateName,
  useTags,
} from "@medatarun/ui/components/business/tag";

export function TagGroupEditPage({ tagGroupId }: { tagGroupId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const tagGroupUpdateName = useTagGroupUpdateName();
  const tagGroupUpdateDescription = useTagGroupUpdateDescription();
  const sec = useSecurityContext();

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;

  const tagGroup = tagsResult.tags.findTagGroup(tagGroupId);
  if (!tagGroup)
    return (
      <ErrorBox error={toProblem(t("tagGroupEdit_notFound", { tagGroupId }))} />
    );

  const actions = actionRegistry.findActionDescriptors([
    "tags/tag_group_update_key",
    "tags/tag_group_delete",
  ]);

  const updateNameDisabled = !sec.canExecuteAction(
    "tags/tag_group_update_name",
  );
  const updateDescriptionDisabled = !sec.canExecuteAction(
    "tags/tag_group_update_description",
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
  const actionCtxPage = createActionCtxTagGroup(tagGroup, displayedSubject);
  const actionCtxTag = (tag: Tag): ActionCtx =>
    createActionCtxTag(tag.scope, displayedSubject, { tag: tag });

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
        disabled={updateNameDisabled}
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
      verticalSpacing={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <InlineEditDescription
        value={tagGroup.description}
        disabled={updateDescriptionDisabled}
        placeholder={t("tagGroupEdit_descriptionPlaceholder")}
        onChange={handleChangeDescription}
      />

      <SectionTitle
        icon={<TagGroupIcon />}
        actions={["tags/tag_global_create"]}
        actionCtx={actionCtxPage}
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
