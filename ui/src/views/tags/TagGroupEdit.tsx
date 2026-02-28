import {useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  type TagSearchItemDto,
  type TagSearchReq,
  useActionRegistry,
  useTagGroupList,
  useTagGroupUpdateDescription,
  useTagGroupUpdateKey,
  useTagGroupUpdateName,
  useTagSearch
} from "../../business";
import {TagGroupIcon, TagIcon} from "../../components/business/Icons.tsx";
import {TagsTable} from "../../components/business/TagsTable.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {
  createActionTemplateTagGroup,
  createActionTemplateTagManagedList
} from "../../components/business/actionTemplates.ts";
import {ViewTitle} from "../../components/core/ViewTitle.tsx";
import {MissingInformation} from "../../components/core/MissingInformation.tsx";
import {InlineEditDescription} from "../../components/core/InlineEditDescription.tsx";
import {InlineEditSingleLine} from "../../components/core/InlineEditSingleLine.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
  tokens
} from "@fluentui/react-components";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable
} from "../../components/layout/Contained.tsx";
import {PropertiesForm} from "../../components/layout/PropertiesForm.tsx";
import {SectionPaper} from "../../components/layout/SectionPaper.tsx";
import {SectionTable} from "../../components/layout/SecionTable.tsx";
import {SectionTitle} from "../../components/layout/SectionTitle.tsx";
import {ViewLayoutContained} from "../../components/layout/ViewLayoutContained.tsx";
import {ErrorBox} from "@seij/common-ui";
import {toProblem} from "@seij/common-types";

const globalTagSearchReq: TagSearchReq = {
  filters: {
    operator: "and" as const,
    items: [
      {
        type: "scopeRef" as const,
        condition: "is" as const,
        value: {type: "global", id: null} as const
      }
    ]
  }
}

export function TagGroupEdit({tagGroupId}: { tagGroupId: string }) {
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const tagGroups = useTagGroupList()
  const tags = useTagSearch(globalTagSearchReq)
  const tagGroupUpdateName = useTagGroupUpdateName()
  const tagGroupUpdateDescription = useTagGroupUpdateDescription()
  const tagGroupUpdateKey = useTagGroupUpdateKey()

  if (tagGroups.isPending || tags.isPending) return null
  if (tagGroups.error) return <ErrorBox error={toProblem(tagGroups.error)}/>
  if (tags.error) return <ErrorBox error={toProblem(tags.error)}/>

  const tagGroup = tagGroups.data?.items.find(item => item.id === tagGroupId)
  if (!tagGroup) return <ErrorBox error={toProblem(`Can not find tag group [${tagGroupId}]`)}/>

  const groupTags: TagSearchItemDto[] = (tags.data?.items ?? []).filter(item => item.groupId === tagGroup.id)
  const actions = actionRegistry.findActions(ActionUILocations.tag_managed_group_detail)

  const handleClickTagGroups = () => {
    navigate({to: "/tag-groups"})
  }

  const handleClickTag = (tagId: string) => {
    navigate({
      to: "/tags/$tagId",
      params: {tagId: tagId}
    })
  }

  const handleChangeName = (value: string) => {
    return tagGroupUpdateName.mutateAsync({tagGroupId: tagGroup.id, value: value})
  }

  const handleChangeDescription = (value: string) => {
    return tagGroupUpdateDescription.mutateAsync({tagGroupId: tagGroup.id, value: value})
  }

  const handleChangeKey = (value: string) => {
    return tagGroupUpdateKey.mutateAsync({tagGroupId: tagGroup.id, value: value})
  }

  return <ViewLayoutContained title={
    <div>
      <div style={{marginLeft: "-22px"}}>
        <Breadcrumb size="small">
          <BreadcrumbItem>
            <BreadcrumbButton
              icon={<TagGroupIcon/>}
              onClick={handleClickTagGroups}>
              Tag groups
            </BreadcrumbButton>
          </BreadcrumbItem>
          <BreadcrumbDivider/>
        </Breadcrumb>
      </div>
      <ViewTitle eyebrow={"Global tag group"}>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div style={{width: "100%"}}>
            <InlineEditSingleLine
              value={tagGroup.name ?? ""}
              onChange={handleChangeName}>
              {tagGroup.name ? tagGroup.name : <MissingInformation>{tagGroup.key}</MissingInformation>} {" "}
            </InlineEditSingleLine>
          </div>
          <div>
            <ActionMenuButton
              label="Actions"
              itemActions={actions}
              actionParams={createActionTemplateTagGroup(tagGroup.id)}
            />
          </div>
        </div>
      </ViewTitle>
    </div>
  }>
    <ContainedMixedScrolling>
      <ContainedScrollable>
        <ContainedHumanReadable>
          <SectionPaper>
            <TagGroupOverview
              tagGroupId={tagGroup.id}
              tagGroupKey={tagGroup.key}
              onChangeKey={handleChangeKey}
            />
          </SectionPaper>
          <SectionPaper topspacing="XXXL" nopadding>
            <InlineEditDescription
              value={tagGroup.description}
              placeholder={"add description"}
              onChange={handleChangeDescription}
            />
          </SectionPaper>

          <SectionTitle
            icon={<TagIcon/>}
            location={ActionUILocations.tag_managed_list}
            actionParams={createActionTemplateTagManagedList(tagGroup.id)}>
            Tags
          </SectionTitle>

          <SectionTable>
            <TagsTable tags={groupTags} onClick={handleClickTag}/>
          </SectionTable>
        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

function TagGroupOverview({tagGroupId, tagGroupKey, onChangeKey}: {
  tagGroupId: string,
  tagGroupKey: string,
  onChangeKey: (value: string) => Promise<unknown>
}) {
  return <PropertiesForm>
    <div><Text>Group&nbsp;key</Text></div>
    <div>
      <InlineEditSingleLine
        value={tagGroupKey}
        onChange={onChangeKey}>
        <Text><code>{tagGroupKey}</code></Text>
      </InlineEditSingleLine>
    </div>

    <div><Text>Identifier</Text></div>
    <div><Text><code>{tagGroupId}</code></Text></div>
  </PropertiesForm>
}
