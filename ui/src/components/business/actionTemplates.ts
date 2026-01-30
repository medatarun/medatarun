import type {ActionPerformerRequestParam, ActionPerformerRequestParams} from "./ActionPerformer.tsx";

const refid = (id: string):  ActionPerformerRequestParam => ({value: "id:" +id, readonly: true})

export const createActionTemplateGeneral = () => ({})
export const createActionTemplateEntity = (modelId: string, entityId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    entityRef: refid(entityId),
  }
}
export const createActionTemplateEntityAttribute = (modelId: string, entityId: string, attributeId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    entityRef: refid(entityId),
    attributeRef: refid(attributeId)
  }
}
export const createActionTemplateRelationshipAttribute = (modelId: string, relationshipId: string, attributeId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    relationshipRef: refid(relationshipId),
    attributeRef: refid(attributeId)
  }
}
export const createActionTemplateModel = (modelId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId)
  }
}
export const createActionTemplateType = (modelId: string, typeId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    typeRef: refid(typeId),
  }
}
export const createActionTemplateRelationship = (modelId: string, relationshipId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    relationshipRef: refid(relationshipId),
  }
}
export const createActionTemplateEntityForRelationships = (modelId: string, entityId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    roleAEntityRef: refid(entityId),
  }
}
export const createActionTemplateRelationshipRole = (modelId: string, relationshipId: string, roleId: string): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    relationshipRef: refid(relationshipId),
    relationshipRoleRef: refid(roleId),
  }
}