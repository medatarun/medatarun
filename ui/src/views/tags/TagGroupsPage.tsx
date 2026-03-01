import {useNavigate} from "@tanstack/react-router";
import { ActionUILocations, useActionRegistry } from "@/business/action_registry";
import { useTags } from "@/business/tag";
import {TagGroupsTable} from "@/components/business/tag/TagGroupsTable.tsx";
import {ActionMenuButton} from "@/components/business/model/TypesTable.tsx";
import {ViewTitle} from "@/components/core/ViewTitle.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "@/components/layout/Contained.tsx";
import {SectionPaper} from "@/components/layout/SectionPaper.tsx";
import {SectionTable} from "@/components/layout/SecionTable.tsx";
import {SectionTitle} from "@/components/layout/SectionTitle.tsx";
import {ViewLayoutContained} from "@/components/layout/ViewLayoutContained.tsx";
import {tokens} from "@fluentui/react-components";
import {ErrorBox} from "@seij/common-ui";
import {toProblem} from "@seij/common-types";
import {createActionTemplateTagGroupList} from "@/components/business/tag/tag.actions.ts";
import {TagGroupIcon} from "@/components/business/tag/tag.icons.tsx";

export function TagGroupsPage() {
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const tagsResult = useTags()
  const actions = actionRegistry.findActions(ActionUILocations.tag_managed_group_list)

  const handleClickTagGroup = (tagGroupId: string) => {
    navigate({
      to: "/tag-group/$tagGroupId",
      params: {tagGroupId: tagGroupId}
    })
  }

  if (tagsResult.isPending) return null
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)}/>

  return <ViewLayoutContained title={
    <ViewTitle eyebrow={"Global tags"}>
      <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
        <div>Tag groups</div>
        <div>
          <ActionMenuButton
            label="Actions"
            itemActions={actions}
            actionParams={createActionTemplateTagGroupList()}
          />
        </div>
      </div>
    </ViewTitle>
  }>
    <ContainedMixedScrolling>
      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            Global scope tag groups centralize the managed vocabularies shared across Medatarun.
          </SectionPaper>

          <SectionTitle
            icon={<TagGroupIcon/>}
            location={ActionUILocations.tag_managed_group_list}
            actionParams={createActionTemplateTagGroupList()}>
            Tag groups
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
}
