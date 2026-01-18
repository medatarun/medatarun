export const createActionTemplateEntityAttribute = (modelId: string, entityId: string, attributeId: string) => {
  return {
    modelKey: modelId,
    entityKey: entityId,
    attributeKey: attributeId
  }
}
export const createActionTemplateRelationshipAttribute = (modelId: string, relationshipId: string, attributeId: string) => {
  return {
    modelKey: modelId,
    relationshipKey: relationshipId,
    attributeKey: attributeId
  }
}
export const createActionTemplateModel = (modelId: string) => {
  return  {
    modelKey: modelId
  }
}
export const createActionTemplateType = (modelId: string, typeId: string) => {
  return  {
    modelKey: modelId,
    typeKey: typeId,
  }
}