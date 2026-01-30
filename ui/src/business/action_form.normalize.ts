import type {FormDataType} from "./action_form.types.ts";
import {ActionDescriptor, ActionDescriptorParam, type ActionRegistry} from "./action_registry.biz.tsx";
import type {ActionPayload} from "./action_perform.api.ts";
import {isNil} from "lodash-es";

export function formDataNormalize(actionGroupKey: string, actionKey: string, formData: FormDataType, actionRegistry: ActionRegistry): ActionPayload {
  const action = actionRegistry.findAction(actionGroupKey, actionKey)
  const payload = formDataToPayload(action, formData)
  return payload
}


function formDataToPayload(action: ActionDescriptor, formData: FormDataType): ActionPayload {
  const payload: ActionPayload = {}
  for (const parameter of action.parameters) {
    const name = parameter.name
    const value = formData[name] ?? null
    payload[name] = isNil(value) ? null : normalize(parameter, value)
  }
  return payload
}

function normalize(param: ActionDescriptorParam, value: unknown) {
  if (param.type == "Boolean") return normalizeBoolean(param, value)
  if (param.type == "AttributeKey") return normalizeKey(param, value)
  if (param.type == "AttributeRef") return normalizeRef(param, value)
  if (param.type == "EntityKey") return normalizeKey(param, value)
  if (param.type == "EntityRef") return normalizeRef(param, value)
  if (param.type == "Hashtag") return normalizeString(param, value)
  if (param.type == "ModelKey") return normalizeKey(param, value)
  if (param.type == "ModelRef") return normalizeRef(param, value)
  if (param.type == "ModelVersion") return normalizeVersion(param, value)
  if (param.type == "RelationshipKey") return normalizeKey(param, value)
  if (param.type == "RelationshipRef") return normalizeRef(param, value)
  if (param.type == "TypeKey") return normalizeKey(param, value)
  if (param.type == "TypeRef") return normalizeRef(param, value)
  if (param.type == "String") return normalizeString(param, value)
  if (param.type == "LocalizedText") return normalizeString(param, value)
  if (param.type == "LocalizedMarkdown") return normalizeString(param, value)
  if (param.type == "RelationshipRoleKey") return normalizeKey(param, value)
  if (param.type == "RelationshipCardinality") return normalizeString(param, value)
  throw Error("Unsupported type: " + param.type)

}

function normalizeBoolean(param: ActionDescriptorParam, value: unknown) {
  if (value === null && param.optional) return null
  if (value === undefined && param.optional) return null
  return value === "true"
}

function normalizeKey(param: ActionDescriptorParam, value: unknown) {
  return normalizeString(param, value)
}

function normalizeRef(param: ActionDescriptorParam, value: unknown) {
  return normalizeString(param, value)
}

function normalizeString(param: ActionDescriptorParam, value: unknown) {
  if (value === null && param.optional) return null
  if (value === undefined && param.optional) return null
  if (value === "") return null
  return value
}

function normalizeVersion(param: ActionDescriptorParam, value: unknown) {
  return normalizeString(param, value)
}
