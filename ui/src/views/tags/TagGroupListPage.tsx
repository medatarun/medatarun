import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { useTags } from "@/business/tag";
import { TagGroupsTable } from "./TagGroupsTable.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { createActionTemplateTagGroupList } from "@/components/business/tag/tag.actions.ts";
import { TagGroupIcon } from "@/components/business/tag/tag.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutPageInfo } from "@/components/layout/ViewLayoutPageInfo.tsx";

export function TagGroupListPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const actions = actionRegistry.findActions(ActionUILocations.tag_group_list);

  const handleClickTagGroup = (tagGroupId: string) => {
    navigate({
      to: "/tag-group/$tagGroupId",
      params: { tagGroupId: tagGroupId },
    });
  };

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;
  const headerProps: ViewLayoutHeaderProps = {
    title: t("tagGroupsPage_title"),
    titleIcon: <TagGroupIcon />,
    eyebrow: t("tagGroupsPage_eyebrow"),
    actions: {
      label: t("tagGroupsPage_actions"),
      itemActions: actions,
      actionParams: createActionTemplateTagGroupList(),
      displayedSubject: displaySubjectNone,
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
        location={ActionUILocations.tag_group_list}
        actionParams={createActionTemplateTagGroupList()}
        displayedSubject={displaySubjectNone}
      >
        {t("tagGroupsPage_sectionTitle")}
      </SectionTitle>

      <SectionTable>
        <TagGroupsTable
          tagGroups={tagsResult.tags.listTagGroups()}
          onClick={handleClickTagGroup}
        />
      </SectionTable>
    </ViewLayoutContained>
  );
}
