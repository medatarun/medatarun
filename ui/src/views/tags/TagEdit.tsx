import {useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  type TagSearchItemDto,
  type TagSearchReq,
  useActionRegistry,
  useTagGroupList,
  useTagManagedUpdateDescription,
  useTagManagedUpdateName,
  useTagSearch
} from "../../business";
import {TagGroupIcon, TagIcon} from "../../components/business/Icons.tsx";
import {ActionMenuButton} from "../../components/business/TypesTable.tsx";
import {createActionTemplateTag} from "../../components/business/actionTemplates.ts";
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

export function TagEdit({tagId}: { tagId: string }) {
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const tagGroups = useTagGroupList()
  const tags = useTagSearch(globalTagSearchReq)
  const tagManagedUpdateName = useTagManagedUpdateName()
  const tagManagedUpdateDescription = useTagManagedUpdateDescription()

  if (tagGroups.isPending || tags.isPending) return null
  if (tagGroups.error) return <ErrorBox error={toProblem(tagGroups.error)}/>
  if (tags.error) return <ErrorBox error={toProblem(tags.error)}/>

  const tag = tags.data?.items.find(item => item.id === tagId)
  if (!tag) return <ErrorBox error={toProblem(`Can not find tag [${tagId}]`)}/>

  const tagGroup = tagGroups.data?.items.find(item => item.id === tag.groupId)
  if (!tagGroup) return <ErrorBox error={toProblem(`Can not find tag group for tag [${tagId}]`)}/>

  const actions = actionRegistry.findActions(ActionUILocations.tag_managed_detail)

  const handleClickTagGroups = () => {
    navigate({to: "/tag-groups"})
  }

  const handleClickTagGroup = () => {
    navigate({
      to: "/tag-group/$tagGroupId",
      params: {tagGroupId: tagGroup.id}
    })
  }

  const handleChangeName = (value: string) => {
    return tagManagedUpdateName.mutateAsync({tagId: tag.id, value: value})
  }

  const handleChangeDescription = (value: string) => {
    return tagManagedUpdateDescription.mutateAsync({tagId: tag.id, value: value})
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
          <BreadcrumbItem>
            <BreadcrumbButton
              icon={<TagIcon/>}
              onClick={handleClickTagGroup}>
              {tagGroup.name ?? tagGroup.key}
            </BreadcrumbButton>
          </BreadcrumbItem>
          <BreadcrumbDivider/>
        </Breadcrumb>
      </div>
      <ViewTitle eyebrow={"Global tag"}>
        <div style={{display: "flex", justifyContent: "space-between", paddingRight: tokens.spacingHorizontalL}}>
          <div style={{width: "100%"}}>
            <InlineEditSingleLine
              value={tag.name ?? ""}
              onChange={handleChangeName}>
              {tag.name ? tag.name : <MissingInformation>{tag.key}</MissingInformation>} {" "}
            </InlineEditSingleLine>
          </div>
          <div>
            <ActionMenuButton
              label="Actions"
              itemActions={actions}
              actionParams={createActionTemplateTag(tag.id)}
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
            <TagOverview tag={tag}/>
          </SectionPaper>
          <SectionPaper topspacing="XXXL" nopadding>
            <InlineEditDescription
              value={tag.description}
              placeholder={"add description"}
              onChange={handleChangeDescription}
            />
          </SectionPaper>
        </ContainedHumanReadable>
      </ContainedScrollable>
    </ContainedMixedScrolling>
  </ViewLayoutContained>
}

function TagOverview({tag}: { tag: TagSearchItemDto }) {
  return <PropertiesForm>
    <div><Text>Scope</Text></div>
    <div><Text>Global</Text></div>

    <div><Text>Key</Text></div>
    <div><Text><code>{tag.key}</code></Text></div>

    <div><Text>Identifier</Text></div>
    <div><Text><code>{tag.id}</code></Text></div>
  </PropertiesForm>
}
