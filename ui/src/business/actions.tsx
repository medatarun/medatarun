type ModelKey = string
type EntityKey = string
type AttributeKey = string
export function batch_Run(props:{actions:string}) {
  return {
    "action": "batch/Run",
    "payload": props
  }
}
export function config_AIAgentsInstructions(props:Record<string, never>) {
  return {
    "action": "config/AIAgentsInstructions",
    "payload": props
  }
}
export function config_Inspect(props:Record<string, never>) {
  return {
    "action": "config/Inspect",
    "payload": props
  }
}
export function config_InspectJson(props:Record<string, never>) {
  return {
    "action": "config/InspectJson",
    "payload": props
  }
}
export function model_EntityAttribute_AddTag(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey, tag:string}) {
  return {
    "action": "model/EntityAttribute_AddTag",
    "payload": props
  }
}
export function model_EntityAttribute_Create(props:{attributeKey:AttributeKey, description:string, entityKey:EntityKey, modelKey:ModelKey, name:string, optional:string, type:string}) {
  return {
    "action": "model/EntityAttribute_Create",
    "payload": props
  }
}
export function model_EntityAttribute_Delete(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey}) {
  return {
    "action": "model/EntityAttribute_Delete",
    "payload": props
  }
}
export function model_EntityAttribute_DeleteTag(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey, tag:string}) {
  return {
    "action": "model/EntityAttribute_DeleteTag",
    "payload": props
  }
}
export function model_EntityAttribute_UpdateDescription(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/EntityAttribute_UpdateDescription",
    "payload": props
  }
}
export function model_EntityAttribute_UpdateId(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/EntityAttribute_UpdateId",
    "payload": props
  }
}
export function model_EntityAttribute_UpdateName(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/EntityAttribute_UpdateName",
    "payload": props
  }
}
export function model_EntityAttribute_UpdateOptional(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/EntityAttribute_UpdateOptional",
    "payload": props
  }
}
export function model_EntityAttribute_UpdateType(props:{attributeKey:AttributeKey, entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/EntityAttribute_UpdateType",
    "payload": props
  }
}
export function model_Entity_AddTag(props:{entityKey:EntityKey, modelKey:ModelKey, tag:string}) {
  return {
    "action": "model/Entity_AddTag",
    "payload": props
  }
}
export function model_Entity_Create(props:{description:string, documentationHome:string, entityKey:EntityKey, identityAttributeKey:AttributeKey, identityAttributeName:string, identityAttributeType:string, modelKey:ModelKey, name:string}) {
  return {
    "action": "model/Entity_Create",
    "payload": props
  }
}
export function model_Entity_Delete(props:{entityKey:EntityKey, modelKey:ModelKey}) {
  return {
    "action": "model/Entity_Delete",
    "payload": props
  }
}
export function model_Entity_DeleteTag(props:{entityKey:EntityKey, modelKey:ModelKey, tag:string}) {
  return {
    "action": "model/Entity_DeleteTag",
    "payload": props
  }
}
export function model_Entity_UpdateDescription(props:{entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/Entity_UpdateDescription",
    "payload": props
  }
}
export function model_Entity_UpdateId(props:{entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/Entity_UpdateId",
    "payload": props
  }
}
export function model_Entity_UpdateName(props:{entityKey:EntityKey, modelKey:ModelKey, value:string}) {
  return {
    "action": "model/Entity_UpdateName",
    "payload": props
  }
}
export function model_Import(props:{from:string}) {
  return {
    "action": "model/Import",
    "payload": props
  }
}
export function model_Inspect_Human(props:Record<string, never>) {
  return {
    "action": "model/Inspect_Human",
    "payload": props
  }
}
export function model_Inspect_Json(props:Record<string, never>) {
  return {
    "action": "model/Inspect_Json",
    "payload": props
  }
}
export function model_Model_AddTag(props:{modelKey:ModelKey, tag:string}) {
  return {
    "action": "model/Model_AddTag",
    "payload": props
  }
}
export function model_Model_Create(props:{description:string, modelKey:ModelKey, name:string, version:string}) {
  return {
    "action": "model/Model_Create",
    "payload": props
  }
}
export function model_Model_Delete(props:{modelKey:ModelKey}) {
  return {
    "action": "model/Model_Delete",
    "payload": props
  }
}
export function model_Model_DeleteTag(props:{modelKey:ModelKey, tag:string}) {
  return {
    "action": "model/Model_DeleteTag",
    "payload": props
  }
}
export function model_Model_UpdateDescription(props:{description:string, modelKey:ModelKey}) {
  return {
    "action": "model/Model_UpdateDescription",
    "payload": props
  }
}
export function model_Model_UpdateName(props:{modelKey:ModelKey, name:string}) {
  return {
    "action": "model/Model_UpdateName",
    "payload": props
  }
}
export function model_Model_UpdateVersion(props:{modelKey:ModelKey, version:string}) {
  return {
    "action": "model/Model_UpdateVersion",
    "payload": props
  }
}
export function model_RelationshipAttribute_AddTag(props:{attributeKey:AttributeKey, modelKey:ModelKey, relationshipKey:string, tag:string}) {
  return {
    "action": "model/RelationshipAttribute_AddTag",
    "payload": props
  }
}
export function model_RelationshipAttribute_Create(props:{attr:string, modelKey:ModelKey, relationshipKey:string}) {
  return {
    "action": "model/RelationshipAttribute_Create",
    "payload": props
  }
}
export function model_RelationshipAttribute_Delete(props:{attributeKey:AttributeKey, modelKey:ModelKey, relationshipKey:string}) {
  return {
    "action": "model/RelationshipAttribute_Delete",
    "payload": props
  }
}
export function model_RelationshipAttribute_DeleteTag(props:{attributeKey:AttributeKey, modelKey:ModelKey, relationshipKey:string, tag:string}) {
  return {
    "action": "model/RelationshipAttribute_DeleteTag",
    "payload": props
  }
}
export function model_RelationshipAttribute_Update(props:{attributeKey:AttributeKey, cmd:string, modelKey:ModelKey, relationshipKey:string}) {
  return {
    "action": "model/RelationshipAttribute_Update",
    "payload": props
  }
}
export function model_Relationship_AddTag(props:{modelKey:ModelKey, relationshipKey:string, tag:string}) {
  return {
    "action": "model/Relationship_AddTag",
    "payload": props
  }
}
export function model_Relationship_Create(props:{initializer:string, modelKey:ModelKey}) {
  return {
    "action": "model/Relationship_Create",
    "payload": props
  }
}
export function model_Relationship_Delete(props:{modelKey:ModelKey, relationshipKey:string}) {
  return {
    "action": "model/Relationship_Delete",
    "payload": props
  }
}
export function model_Relationship_DeleteTag(props:{modelKey:ModelKey, relationshipKey:string, tag:string}) {
  return {
    "action": "model/Relationship_DeleteTag",
    "payload": props
  }
}
export function model_Relationship_Update(props:{cmd:string, modelKey:ModelKey, relationshipKey:string}) {
  return {
    "action": "model/Relationship_Update",
    "payload": props
  }
}
export function model_Type_Create(props:{description:string, modelKey:ModelKey, name:string, typeKey:string}) {
  return {
    "action": "model/Type_Create",
    "payload": props
  }
}
export function model_Type_Delete(props:{modelKey:ModelKey, typeKey:string}) {
  return {
    "action": "model/Type_Delete",
    "payload": props
  }
}
export function model_Type_UpdateDescription(props:{description:string, modelKey:ModelKey, typeKey:string}) {
  return {
    "action": "model/Type_UpdateDescription",
    "payload": props
  }
}
export function model_Type_UpdateName(props:{modelKey:ModelKey, name:string, typeKey:string}) {
  return {
    "action": "model/Type_UpdateName",
    "payload": props
  }
}
