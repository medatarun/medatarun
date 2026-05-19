import { useNavigate } from "@tanstack/react-router";
import { createActionCtxTagGroup, TagGroup } from "@medatarun/ui/business/tag";
import { useTags } from "@medatarun/ui/components/business/tag";
import { TagGroupsTable } from "./TagGroupsTable.tsx";
import { SectionTable } from "@medatarun/ui/components/layout/SecionTable.tsx";
import { SectionTitle } from "@medatarun/ui/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { TagGroupIcon } from "@medatarun/ui/components/business/tag/tag.icons.tsx";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import {
  createActionCtxVoid,
  displaySubjectNone,
} from "@medatarun/ui/business/action-performer";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader.tsx";
import { useActionRegistry } from "@medatarun/ui/components/business/actions";
import { MessageBox } from "@medatarun/ui/components/core/MessageBox.tsx";
import { Markdown } from "@medatarun/ui/components/core/Markdown.tsx";

export function TagGroupListPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const actions = actionRegistry.findActionDescriptors([
    "tags/tag_group_create",
  ]);

  const handleClickTagGroup = (tagGroupId: string) => {
    navigate({
      to: "/tag-group/$tagGroupId",
      params: { tagGroupId: tagGroupId },
    });
  };

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;

  const actionCtxPage = createActionCtxVoid();

  const actionCtxTagGroup = (tagGroup: TagGroup) =>
    createActionCtxTagGroup(tagGroup, displaySubjectNone);

  const headerProps: ViewLayoutHeaderProps = {
    title: t("tagGroupsPage_title"),
    titleIcon: <TagGroupIcon />,
    eyebrow: t("tagGroupsPage_eyebrow"),
    actions: {
      label: t("tagGroupsPage_actions"),
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
      <MessageBox intent={"info"}>
        <Markdown value={t("tagGroupsPage_description")} />
      </MessageBox>

      <SectionTitle
        icon={<TagGroupIcon />}
        actions={["tags/tag_group_create"]}
        actionCtx={actionCtxPage}
      >
        {t("tagGroupsPage_sectionTitle")}
      </SectionTitle>

      <SectionTable>
        <TagGroupsTable
          tagGroups={tagsResult.tags.listTagGroups()}
          onClick={handleClickTagGroup}
          actionCtxTagGroup={actionCtxTagGroup}
        />
      </SectionTable>
    </ViewLayoutContained>
  );
}
