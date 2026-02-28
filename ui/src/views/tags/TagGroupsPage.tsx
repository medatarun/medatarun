import {useNavigate} from "@tanstack/react-router";
import {ActionUILocations, useActionRegistry, useTagGroupList} from "../../business";
import {TagGroupIcon} from "../../components/business/Icons.tsx";
import {TagGroupsTable} from "../../components/business/TagGroupsTable.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {createActionTemplateTagGroupList} from "../../components/business/actionTemplates.ts";
import {ViewTitle} from "../../components/core/ViewTitle.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "../../components/layout/Contained.tsx";
import {SectionPaper} from "../../components/layout/SectionPaper.tsx";
import {SectionTable} from "../../components/layout/SecionTable.tsx";
import {SectionTitle} from "../../components/layout/SectionTitle.tsx";
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";
import {tokens} from "@fluentui/react-components";
import {ErrorBox} from "@seij/common-ui";
import {toProblem} from "@seij/common-types";

export function TagGroupsPage() {
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const tagGroups = useTagGroupList()
  const actions = actionRegistry.findActions(ActionUILocations.tag_managed_group_list)

  const handleClickTagGroup = (tagGroupId: string) => {
    navigate({
      to: "/tag-group/$tagGroupId",
      params: {tagGroupId: tagGroupId}
    })
  }

  if (tagGroups.isPending) return null
  if (tagGroups.error) return <ErrorBox error={toProblem(tagGroups.error)}/>

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
              tagGroups={tagGroups.data?.items ?? []}
              onClick={handleClickTagGroup}
            />
          </SectionTable>
        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}
