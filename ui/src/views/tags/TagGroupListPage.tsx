import { useNavigate } from "@tanstack/react-router";
import { createActionCtxTagGroup, TagGroup } from "@/business/tag";
import { useTags } from "@/components/business/tag";
import { TagGroupsTable } from "./TagGroupsTable.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { TagGroupIcon } from "@/components/business/tag/tag.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  createActionCtxVoid,
  displaySubjectNone,
} from "@/business/action-performer";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutPageInfo } from "@/components/layout/ViewLayoutPageInfo.tsx";
import { useActionRegistry } from "@/components/business/actions";

export function TagGroupListPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const actions = actionRegistry.findActionDescriptors(["tag_group_create"]);

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
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <ViewLayoutPageInfo>{t("tagGroupsPage_description")}</ViewLayoutPageInfo>

      <SectionTitle
        icon={<TagGroupIcon />}
        actions={["tag_group_create"]}
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
