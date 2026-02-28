import {InteractionTag, InteractionTagPrimary, makeStyles, TagGroup, tokens} from "@fluentui/react-components";
import {TagList, type TagScopeRef, useTagGroupList, useTagSearch} from "../../business";


const useStyles = makeStyles(
  {
    wrappingTagGroup: {
      flexWrap: "wrap", display: "flex", rowGap: tokens.spacingVerticalS
    }
  }
)

export function Tag({label}: { label: string }) {
  return <InteractionTag>
    <InteractionTagPrimary>{label}</InteractionTagPrimary>
  </InteractionTag>
}

const globalTagScope: TagScopeRef = {type: "global", id: null}

function tagSearchByScope(scope: TagScopeRef) {
  return {
    filters: {
      operator: "and" as const,
      items: [
        {
          type: "scopeRef" as const,
          condition: "is" as const,
          value: scope
        }
      ]
    }
  }
}

export function modelTagScope(modelId: string): TagScopeRef {
  return {type: "model", id: modelId}
}

export function Tags({tags, scope}: { tags: string[], scope: TagScopeRef }) {
  const styles = useStyles()
  const globalTags = useTagSearch(tagSearchByScope(globalTagScope))
  const scopedTags = useTagSearch(tagSearchByScope(scope), scope.type !== "global")
  const tagGroups = useTagGroupList()

  const tagList = new TagList(
    [...(globalTags.data?.items ?? []), ...(scopedTags.data?.items ?? [])],
    tagGroups.data?.items ?? []
  )

  return <TagGroup className={styles.wrappingTagGroup}>
    {tags.map((it, i) => <Tag key={i} label={tagList.formatLabel(it)}/>)}
  </TagGroup>
}
