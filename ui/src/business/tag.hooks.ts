import {useMemo} from "react";
import {useTagGroupList, useTagSearch, type TagScopeRef} from "./action_perform.hooks.ts";
import {Tags} from "./tag.ts";

const globalTagScope: TagScopeRef = {type: "global", id: null}
const EMPTY_TABLE: never[] = []

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

/**
 * Loads the tag registry used by the UI.
 * Without a scope, it loads every known tag.
 * With a scope, it loads global tags plus the tags of that local scope.
 */
export function useTags(scope?: TagScopeRef): { tags: Tags, isPending: boolean, error: unknown } {
  const allTagsEnabled = scope === undefined
  const globalTagsEnabled = scope !== undefined
  const scopedTagsEnabled = scope !== undefined && scope.type !== "global"

  const allTags = useTagSearch({}, allTagsEnabled)
  const globalTags = useTagSearch(tagSearchByScope(globalTagScope), globalTagsEnabled)
  const scopedTags = useTagSearch(tagSearchByScope(scope ?? globalTagScope), scopedTagsEnabled)
  const tagGroups = useTagGroupList()

  const allTagDtos = allTags.data?.items ?? EMPTY_TABLE
  const globalTagDtos = globalTags.data?.items ?? EMPTY_TABLE
  const scopedTagDtos = scopedTags.data?.items ?? EMPTY_TABLE
  const groupDtos = tagGroups.data?.items ?? EMPTY_TABLE

  const tags = useMemo(() => {
    const tagDtos = scope === undefined
      ? allTagDtos
      : [...globalTagDtos, ...scopedTagDtos]
    return new Tags(tagDtos, groupDtos)
  }, [allTagDtos, globalTagDtos, groupDtos, scope, scopedTagDtos])

  return {
    tags: tags,
    isPending: (allTagsEnabled && allTags.isPending)
      || (globalTagsEnabled && globalTags.isPending)
      || (scopedTagsEnabled && scopedTags.isPending)
      || tagGroups.isPending,
    error: allTags.error ?? globalTags.error ?? scopedTags.error ?? tagGroups.error
  }
}
