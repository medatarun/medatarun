import {Link, useNavigate} from "@tanstack/react-router";
import {
  ActionUILocations,
  Model,
  type TagSearchItemDto,
  useActionRegistry,
  useTagGroupList,
  useTagFreeUpdateDescription,
  useTagFreeUpdateName,
  useTagManagedUpdateDescription,
  useTagManagedUpdateName,
  useTagSearch,
  useModel
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

export function TagEdit({tagId}: { tagId: string }) {
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const tagGroups = useTagGroupList()
  const tags = useTagSearch({})
  const tagManagedUpdateName = useTagManagedUpdateName()
  const tagManagedUpdateDescription = useTagManagedUpdateDescription()
  const tagFreeUpdateName = useTagFreeUpdateName()
  const tagFreeUpdateDescription = useTagFreeUpdateDescription()

  if (tagGroups.isPending || tags.isPending) return null
  if (tagGroups.error) return <ErrorBox error={toProblem(tagGroups.error)}/>
  if (tags.error) return <ErrorBox error={toProblem(tags.error)}/>

  const tag = tags.data?.items.find(item => item.id === tagId)
  if (!tag) return <ErrorBox error={toProblem(`Can not find tag [${tagId}]`)}/>

  const tagGroup = tagGroups.data?.items.find(item => item.id === tag.groupId)
  const isGlobalTag = tag.tagScopeRef.type === "global"
  if (isGlobalTag && !tagGroup) return <ErrorBox error={toProblem(`Can not find tag group for tag [${tagId}]`)}/>

  const actions = actionRegistry.findActions(
    isGlobalTag ? ActionUILocations.tag_managed_detail : ActionUILocations.tag_free_detail
  )

  const handleClickTagGroups = () => {
    navigate({to: "/tag-groups"})
  }

  const handleClickTagGroup = () => {
    if (!tagGroup) {
      return
    }
    navigate({
      to: "/tag-group/$tagGroupId",
      params: {tagGroupId: tagGroup.id}
    })
  }

  const handleChangeName = (value: string) => {
    if (isGlobalTag) {
      return tagManagedUpdateName.mutateAsync({tagId: tag.id, value: value})
    }
    return tagFreeUpdateName.mutateAsync({tagId: tag.id, value: value})
  }

  const handleChangeDescription = (value: string) => {
    if (isGlobalTag) {
      return tagManagedUpdateDescription.mutateAsync({tagId: tag.id, value: value})
    }
    return tagFreeUpdateDescription.mutateAsync({tagId: tag.id, value: value})
  }

  return <ViewLayoutContained title={
    <div>
      <div style={{marginLeft: "-22px"}}>
        <Breadcrumb size="small">
          <TagEditBreadcrumb
            tag={tag}
            tagGroupLabel={tagGroup ? (tagGroup.name ?? tagGroup.key) : null}
            onClickTagGroups={handleClickTagGroups}
            onClickTagGroup={handleClickTagGroup}
          />
        </Breadcrumb>
      </div>
      <ViewTitle eyebrow={isGlobalTag ? "Global tag" : "Local tag"}>
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

function TagEditBreadcrumb({tag, tagGroupLabel, onClickTagGroups, onClickTagGroup}: {
  tag: TagSearchItemDto,
  tagGroupLabel: string | null,
  onClickTagGroups: () => void,
  onClickTagGroup: () => void
}) {
  const modelId = tag.tagScopeRef.type === "model" ? tag.tagScopeRef.id : null

  if (tag.tagScopeRef.type === "global") {
    return <>
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<TagGroupIcon/>}
          onClick={onClickTagGroups}>
          Tag groups
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider/>
      {tagGroupLabel &&
        <>
          <BreadcrumbItem>
            <BreadcrumbButton
              icon={<TagIcon/>}
              onClick={onClickTagGroup}>
              {tagGroupLabel}
            </BreadcrumbButton>
          </BreadcrumbItem>
          <BreadcrumbDivider/>
        </>
      }
    </>
  }

  if (modelId) {
    return <LocalModelBreadcrumb modelId={modelId}/>
  }

  return <>
    <BreadcrumbItem>
      <Text>{tag.tagScopeRef.type}</Text>
    </BreadcrumbItem>
    <BreadcrumbDivider/>
  </>
}

function LocalModelBreadcrumb({modelId}: { modelId: string }) {
  const {data: modelDto} = useModel(modelId)
  const model = modelDto ? new Model(modelDto) : null

  if (!model) {
    return <>
      <BreadcrumbItem>
        <Text>model</Text>
      </BreadcrumbItem>
      <BreadcrumbDivider/>
    </>
  }

  return <>
    <BreadcrumbItem>
      <Link to="/model/$modelId" params={{modelId: modelId}}>{model.nameOrKey}</Link>
    </BreadcrumbItem>
    <BreadcrumbDivider/>
  </>
}

function TagOverview({tag}: { tag: TagSearchItemDto }) {
  return <PropertiesForm>
    <div><Text>Scope</Text></div>
    <div><Text>{tag.tagScopeRef.type === "global" ? "Global" : `${tag.tagScopeRef.type} / ${tag.tagScopeRef.id}`}</Text></div>

    <div><Text>Key</Text></div>
    <div><Text><code>{tag.key}</code></Text></div>

    <div><Text>Identifier</Text></div>
    <div><Text><code>{tag.id}</code></Text></div>
  </PropertiesForm>
}
