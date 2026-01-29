import {useMutation, useQueryClient} from "@tanstack/react-query";
import {executeAction} from "./action_perform.api.ts";

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
export const useEntityUpdateDescription = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (props: { modelId: string, entityId: string, value: string }) =>
      executeAction("model", "entity_update_description", {
        modelRef: "id:" + props.modelId,
        entityRef: "id:" + props.entityId,
        value: props.value
      }),
    onSuccess: () => queryClient.invalidateQueries()

  })
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