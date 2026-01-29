import {useMutation, useQueryClient} from "@tanstack/react-query";
import {type ActionPayload, executeAction} from "./action_perform.api.ts";

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

export const useTypeUpdateDescription = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, typeId: string, value: string }) =>
      executeAction("model", "type_update_description", {
        modelRef: "id:" + props.modelId,
        typeRef: "id:" + props.typeId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()

  })
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

export const useEntityAttributeUpdateDescription = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, entityId: string, attributeId: string, value: string }) =>
      executeAction("model", "entity_attribute_update_description", {
        modelRef: "id:" + props.modelId,
        entityRef: "id:" + props.entityId,
        attributeRef: "id:" + props.attributeId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()

  })
}
export const useRelationshipUpdateDescription = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, relationshipId: string, value: string }) =>
      executeAction("model", "relationship_update_description", {
        modelRef: "id:" + props.modelId,
        relationshipRef: "id:" + props.relationshipId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()

  })
}
export const useRelationshipAttributeUpdateDescription = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, relationshipId: string, attributeId: string, value: string }) =>
      executeAction("model", "relationship_attribute_update_description", {
        modelRef: "id:" + props.modelId,
        relationshippRef: "id:" + props.relationshipId,
        attributeRef: "id:" + props.attributeId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()

  })
}