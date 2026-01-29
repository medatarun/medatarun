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
    mutationFn: (props: { modelId: string, entityId: string, attributeId:string } & P) => {
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

function useRelationshipAttributeMutation<P extends ActionPayload>(actionGroupKey: string, actionKey: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, relationshipId: string, attributeId:string } & P) => {
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



