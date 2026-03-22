import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { useTags } from "@/business/tag";
import { TagGroupsTable } from "./TagGroupsTable.tsx";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { tokens } from "@fluentui/react-components";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { createActionTemplateTagGroupList } from "@/components/business/tag/tag.actions.ts";
import { TagGroupIcon } from "@/components/business/tag/tag.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";

export function TagGroupsPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags();
  const actions = actionRegistry.findActions(
    ActionUILocations.tag_group_list,
  );

  const handleClickTagGroup = (tagGroupId: string) => {
    navigate({
      to: "/tag-group/$tagGroupId",
      params: { tagGroupId: tagGroupId },
    });
  };

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;

  return (
    <ViewLayoutContained
      title={
        <ViewTitle eyebrow={t("tagGroupsPage_eyebrow")}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              paddingRight: tokens.spacingHorizontalL,
            }}
          >
            <div>{t("tagGroupsPage_title")}</div>
            <div>
              <ActionMenuButton
                label={t("tagGroupsPage_actions")}
                itemActions={actions}
                actionParams={createActionTemplateTagGroupList()}
                displayedSubject={displaySubjectNone}
              />
            </div>
          </div>
        </ViewTitle>
      }
    >
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <ContainedHumanReadable>
            <SectionPaper>{t("tagGroupsPage_description")}</SectionPaper>

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
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}
