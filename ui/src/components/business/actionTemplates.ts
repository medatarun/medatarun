export const createActionTemplateEntity = (modelId: string, entityId: string) => {
  return {
    modelKey: modelId,
    entityKey: entityId,
  }
}
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
export const createActionTemplateRelationship = (modelId: string, relationshipId: string) => {
  return  {
    modelKey: modelId,
    relationshipKey: relationshipId,
  }
}
export const createActionTemplateEntityForRelationships = (modelId: string,  entityId: string) => {
  return  {
    modelKey: modelId,
    roleAEntityKey: entityId,
  }
}