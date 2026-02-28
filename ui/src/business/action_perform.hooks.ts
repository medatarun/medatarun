import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {type ActionPayload, executeAction} from "./action_perform.api.ts";
import {toProblem} from "@seij/common-types";

export type TagScopeRef =
  | {type: "global", id: null}
  | {type: string, id: string}

export type TagSearchReq = {
  filters?: TagSearchFilters | null
}

export type TagSearchFilters = {
  operator: "and" | "or"
  items: TagSearchFilter[]
}

export type TagSearchFilter = {
  type: "scopeRef"
  condition: "is"
  value: TagScopeRef
}

export type TagSearchItemDto = {
  id: string
  key: string
  groupId: string | null
  tagScopeRef: TagScopeRef
  name: string | null
  description: string | null
}

export type TagSearchResp = {
  items: TagSearchItemDto[]
}

export type TagGroupListItemDto = {
  id: string
  key: string
  name: string | null
  description: string | null
}

export type TagGroupListResp = {
  items: TagGroupListItemDto[]
}

export const useTagSearch = (req: TagSearchReq, enabled: boolean = true) => {
  return useQuery({
    queryKey: ["action", "tag", "tag_search", req.filters ?? null],
    enabled: enabled,
    queryFn: async () => {
      const payload: ActionPayload = {}
      if (req.filters !== undefined) {
        payload.filters = req.filters
      }
      const response = await executeAction<TagSearchResp>("tag", "tag_search", payload)
      if (response.contentType !== "json") {
        throw Error("Expected JSON response for tag/tag_search")
      }
      return response.json
    }
  })
}

export const useTagGroupList = () => {
  return useQuery({
    queryKey: ["action", "tag", "tag_group_list"],
    queryFn: async () => {
      const response = await executeAction<TagGroupListResp>("tag", "tag_group_list", {})
      if (response.contentType !== "json") {
        throw Error("Expected JSON response for tag/tag_group_list")
      }
      return response.json
    }
  })
}

function useTagGroupMutation(actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { tagGroupId: string, value: string }) =>
      executeAction("tag", actionKey, {
        tagGroupRef: "id:" + props.tagGroupId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}

function useManagedTagMutation(actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { tagId: string, value: string }) =>
      executeAction("tag", actionKey, {
        tagRef: "id:" + props.tagId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}

export const useTagGroupUpdateName = () => {
  return useTagGroupMutation("tag_group_update_name")
}

export const useTagGroupUpdateDescription = () => {
  return useTagGroupMutation("tag_group_update_description")
}

export const useTagGroupUpdateKey = () => {
  return useTagGroupMutation("tag_group_update_key")
}

export const useTagManagedUpdateName = () => {
  return useManagedTagMutation("tag_managed_update_name")
}

export const useTagManagedUpdateDescription = () => {
  return useManagedTagMutation("tag_managed_update_description")
}

export interface SearchResultLocation {
  objectType: string
  modelId: string
  modelKey: string
  modelLabel: string
  entityId: string | undefined
  entityKey: string | undefined
  entityLabel: string | undefined
  entityAttributeId: string | undefined
  entityAttributeLabel: string | undefined
  relationshipId: string | undefined
  relationshipLabel: string | undefined
  relationshipAttributeId: string | undefined
  relationshipAttributeLabel: string | undefined
}

export interface SearchResult {
  id: string
  location: SearchResultLocation
  tags: string[]
}

export interface SearchResults {
  items: SearchResult[]
}

export async function modelSearch(tags: string): Promise<SearchResults> {
  if (tags == "") return {items:[]}
  const payload = {
    filters: {
      operator: "and",
      items: [
        {"type": "tags", "condition": "anyOf", "value": tags.split(",").map(it => it.trim())}
      ]
    },
    fields: ["location"],
  }
  const result = await executeAction<SearchResults>("model", "search", payload)
  if (result.contentType === "json") {
    return result.json
  } else throw toProblem("Invalid response content type")
}
export const useModelSearch = (tags: string) => {
  return useQuery({
    queryKey: ["search", tags],
    queryFn: () => modelSearch(tags)
  })
}

export const useModelUpdateName = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, value: string }) =>
      executeAction("model", "model_update_name", {
        modelRef: "id:" + props.modelId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}
export const useModelUpdateDescription = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, value: string }) =>
      executeAction("model", "model_update_description", {
        modelRef: "id:" + props.modelId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}
export const useModelUpdateDocumentationHome = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, value: string }) =>
      executeAction("model", "model_update_documentation_link", {
        modelRef: "id:" + props.modelId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}
export const useModelUpdateKey = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, value: string }) =>
      executeAction("model", "model_update_key", {
        modelRef: "id:" + props.modelId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}
export const useModelUpdateVersion = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, value: string }) =>
      executeAction("model", "model_update_version", {
        modelRef: "id:" + props.modelId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}
export const useModelAddTag = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, tag: string }) =>
      executeAction("model", "model_add_tag", {
        modelRef: "id:" + props.modelId,
        tag: props.tag
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}
export const useModelDeleteTag = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, tag: string }) =>
      executeAction("model", "model_delete_tag", {
        modelRef: "id:" + props.modelId,
        tag: props.tag
      }),
    onSuccess: () => queryClient.invalidateQueries()
  })
}

function useTypeMutation<P extends ActionPayload>(actionGroupKey: string, actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, typeId: string } & P) => {
      const {modelId, typeId, ...otherProps} = props
      return executeAction(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        typeRef: "id:" + typeId,
        ...otherProps
      })
    },
    onSuccess: () => queryClient.invalidateQueries()
  })

}

export const useTypeUpdateName = () => {
  return useTypeMutation<{ value: string }>("model", "type_update_name");
}
export const useTypeUpdateKey = () => {
  return useTypeMutation<{ value: string }>("model", "type_update_key");
}
export const useTypeUpdateDescription = () => {
  return useTypeMutation<{ value: string }>("model", "type_update_description");
}


function useEntityMutation<P extends ActionPayload>(actionGroupKey: string, actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, entityId: string } & P) => {
      const {modelId, entityId, ...otherProps} = props
      return executeAction(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        entityRef: "id:" + entityId,
        ...otherProps
      })
    },
    onSuccess: () => queryClient.invalidateQueries()
  })

}

export const useEntityUpdateName = () => {
  return useEntityMutation<{ value: string }>("model", "entity_update_name");
}
export const useEntityUpdateKey = () => {
  return useEntityMutation<{ value: string }>("model", "entity_update_key");
}
export const useEntityUpdateDescription = () => {
  return useEntityMutation<{ value: string }>("model", "entity_update_description");
}
export const useEntityUpdateDocumentationHome = () => {
  return useEntityMutation<{ value: string }>("model", "entity_update_documentation_link");
}
export const useEntityAddTag = () => {
  return useEntityMutation<{ tag: string }>("model", "entity_add_tag")
}
export const useEntityDeleteTag = () => {
  return useEntityMutation<{ tag: string }>("model", "entity_delete_tag")
}

function useEntityAttributeMutation<P extends ActionPayload>(actionGroupKey: string, actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, entityId: string, attributeId: string } & P) => {
      const {modelId, entityId, attributeId, ...otherProps} = props
      return executeAction(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        entityRef: "id:" + entityId,
        attributeRef: "id:" + attributeId,
        ...otherProps
      })
    },
    onSuccess: () => queryClient.invalidateQueries()
  })
}

export const useEntityAttributeUpdateName = () => {
  return useEntityAttributeMutation<{ value: string }>("model", "entity_attribute_update_name");
}
export const useEntityAttributeUpdateDescription = () => {
  return useEntityAttributeMutation<{ value: string }>("model", "entity_attribute_update_description");
}
export const useEntityAttributeUpdateKey = () => {
  return useEntityAttributeMutation<{ value: string }>("model", "entity_attribute_update_key");
}
export const useEntityAttributeUpdateType = () => {
  return useEntityAttributeMutation<{ value: string }>("model", "entity_attribute_update_type");
}
export const useEntityAttributeUpdateOptional = () => {
  return useEntityAttributeMutation<{ value: boolean }>("model", "entity_attribute_update_optional");
}
export const useEntityAttributeAddTag = () => {
  return useEntityAttributeMutation<{ tag: string }>("model", "entity_attribute_add_tag")
}
export const useEntityAttributeDeleteTag = () => {
  return useEntityAttributeMutation<{ tag: string }>("model", "entity_attribute_delete_tag")
}


function useRelationshipMutation<P extends ActionPayload>(actionGroupKey: string, actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, relationshipId: string } & P) => {
      const {modelId, relationshipId, ...otherProps} = props
      return executeAction(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        relationshipRef: "id:" + relationshipId,
        ...otherProps
      })
    },
    onSuccess: () => queryClient.invalidateQueries()
  })
}

export const useRelationshipUpdateName = () => {
  return useRelationshipMutation<{ value: string }>("model", "relationship_update_name");
}
export const useRelationshipUpdateDescription = () => {
  return useRelationshipMutation<{ value: string }>("model", "relationship_update_description");
}
export const useRelationshipUpdateKey = () => {
  return useRelationshipMutation<{ value: string }>("model", "relationship_update_key");
}
export const useRelationshipAddTag = () => {
  return useRelationshipMutation<{ tag: string }>("model", "relationship_add_tag")
}
export const useRelationshipDeleteTag = () => {
  return useRelationshipMutation<{ tag: string }>("model", "relationship_delete_tag")
}

function useRelationshipAttributeMutation<P extends ActionPayload>(actionGroupKey: string, actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, relationshipId: string, attributeId: string } & P) => {
      const {modelId, relationshipId, attributeId, ...otherProps} = props
      return executeAction(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        relationshipRef: "id:" + relationshipId,
        attributeRef: "id:" + attributeId,
        ...otherProps
      })
    },
    onSuccess: () => queryClient.invalidateQueries()
  })
}

export const useRelationshipAttributeUpdateName = () => {
  return useRelationshipAttributeMutation<{ value: string }>("model", "relationship_attribute_update_name");
}
export const useRelationshipAttributeUpdateDescription = () => {
  return useRelationshipAttributeMutation<{ value: string }>("model", "relationship_attribute_update_description");
}
export const useRelationshipAttributeUpdateKey = () => {
  return useRelationshipAttributeMutation<{ value: string }>("model", "relationship_attribute_update_key");
}
export const useRelationshipAttributeUpdateType = () => {
  return useRelationshipAttributeMutation<{ value: string }>("model", "relationship_attribute_update_type");
}
export const useRelationshipAttributeUpdateOptional = () => {
  return useRelationshipAttributeMutation<{ value: boolean }>("model", "relationship_attribute_update_optional");
}
export const useRelationshipAttributeAddTag = () => {
  return useRelationshipAttributeMutation<{ tag: string }>("model", "relationship_attribute_add_tag")
}
export const useRelationshipAttributeDeleteTag = () => {
  return useRelationshipAttributeMutation<{ tag: string }>("model", "relationship_attribute_delete_tag")
}
