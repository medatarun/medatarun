export type TagScopeRef =
  | { type: "global"; id: null }
  | { type: string; id: string };

export type TagGroupDto = {
  id: string;
  key: string;
  name: string | null;
  description: string | null;
};

export type TagDto = {
  id: string;
  key: string;
  groupId: string | null;
  tagScopeRef: TagScopeRef;
  name: string | null;
  description: string | null;
};

export type TagSearchFilters = {
  operator: "and" | "or";
  items: TagSearchFilter[];
};

export type TagSearchFilter = {
  type: "scopeRef";
  condition: "is";
  value: TagScopeRef;
};
