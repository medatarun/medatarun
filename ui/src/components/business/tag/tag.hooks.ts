import { useMemo } from "react";
import type { TagScopeRef } from "@/business/tag/tag.dto.ts";
import { Tags } from "@/business/tag/tag.domain.ts";
import {
  type TagSearchReq,
  useTagGroupList,
  useTagSearch,
} from "./tag.storage.ts";

const globalTagScope: TagScopeRef = { type: "global", id: null };
const EMPTY_TABLE: never[] = [];

function buildTagSearchReq(scope?: TagScopeRef): TagSearchReq {
  if (scope === undefined) {
    return {};
  }
  if (scope.type === "global") {
    return {
      filters: {
        operator: "and" as const,
        items: [
          {
            type: "scopeRef" as const,
            condition: "is" as const,
            value: scope,
          },
        ],
      },
    };
  }

  return {
    filters: {
      operator: "or" as const,
      items: [
        {
          type: "scopeRef" as const,
          condition: "is" as const,
          value: globalTagScope,
        },
        {
          type: "scopeRef" as const,
          condition: "is" as const,
          value: scope,
        },
      ],
    },
  };
}

/**
 * Loads the tag registry used by the UI.
 * Without a scope, it loads every known tag.
 * With the global scope, it loads only global tags.
 * With a local scope, it loads global tags plus the tags of that local scope.
 */
export function useTags(scope?: TagScopeRef): {
  tags: Tags;
  isPending: boolean;
  error: unknown;
} {
  const tagsQuery = useTagSearch(buildTagSearchReq(scope));
  const tagGroups = useTagGroupList();

  const tagDtos = tagsQuery.data?.items ?? EMPTY_TABLE;
  const groupDtos = tagGroups.data?.items ?? EMPTY_TABLE;

  const tags = useMemo(() => {
    return new Tags(tagDtos, groupDtos);
  }, [groupDtos, tagDtos]);

  return {
    tags: tags,
    isPending: tagsQuery.isPending || tagGroups.isPending,
    error: tagsQuery.error ?? tagGroups.error,
  };
}
