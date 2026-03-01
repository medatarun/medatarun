import {Link, useNavigate} from "@tanstack/react-router";
import {
  Model,
  useActionRegistry,
  type Tag,
  useTags,
  useTagFreeUpdateDescription,
  useTagFreeUpdateName,
  useTagManagedUpdateDescription,
  useTagManagedUpdateName,
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
  const tagsResult = useTags()
  const tagManagedUpdateName = useTagManagedUpdateName()
  const tagManagedUpdateDescription = useTagManagedUpdateDescription()
  const tagFreeUpdateName = useTagFreeUpdateName()
  const tagFreeUpdateDescription = useTagFreeUpdateDescription()

  if (tagsResult.isPending) return null
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)}/>

  const tag = tagsResult.tags.findTag(tagId)
  if (!tag) return <ErrorBox error={toProblem(`Can not find tag [${tagId}]`)}/>

  const isGlobalTag = tag.isGlobal

  const actions = actionRegistry.findActions(tag.detailActionLocation)

  const handleClickTagGroups = () => {
    navigate({to: "/tag-groups"})
  }

  const handleClickTagGroup = () => {
    if (!tag.groupId) {
      return
    }
    navigate({
      to: "/tag-group/$tagGroupId",
      params: {tagGroupId: tag.groupId}
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

function TagEditBreadcrumb({tag, onClickTagGroups, onClickTagGroup}: {
  tag: Tag,
  onClickTagGroups: () => void,
  onClickTagGroup: () => void
}) {
  const modelId = tag.scope.type === "model" ? tag.scope.id : null

  if (tag.isGlobal) {
    return <>
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<TagGroupIcon/>}
          onClick={onClickTagGroups}>
          Tag groups
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider/>
      {tag.groupLabel &&
        <>
          <BreadcrumbItem>
            <BreadcrumbButton
              icon={<TagIcon/>}
              onClick={onClickTagGroup}>
              {tag.groupLabel}
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
      <Text>{tag.scope.type}</Text>
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

function TagOverview({tag}: { tag: Tag }) {
  return <PropertiesForm>
    <div><Text>Scope</Text></div>
    <div><Text>{tag.scopeLabel}</Text></div>

    <div><Text>Key</Text></div>
    <div><Text><code>{tag.key}</code></Text></div>

    <div><Text>Identifier</Text></div>
    <div><Text><code>{tag.id}</code></Text></div>
  </PropertiesForm>
}
