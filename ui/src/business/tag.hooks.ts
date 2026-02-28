import {useTagGroupList, useTagSearch, type TagScopeRef} from "./action_perform.hooks.ts";
import {TagList} from "./tag.ts";

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

export function useTagScopedList(scope: TagScopeRef): { tagList: TagList, isPending: boolean, error: unknown } {
  const globalTags = useTagSearch(tagSearchByScope(globalTagScope))
  const scopedTags = useTagSearch(tagSearchByScope(scope), scope.type !== "global")
  const tagGroups = useTagGroupList()

  return {
    tagList: new TagList(
      [...(globalTags.data?.items ?? []), ...(scopedTags.data?.items ?? [])],
      tagGroups.data?.items ?? []
    ),
    isPending: globalTags.isPending || scopedTags.isPending || tagGroups.isPending,
    error: globalTags.error ?? scopedTags.error ?? tagGroups.error
  }
}
