import type { FormDataType } from "./action_form.types.ts";
import {
  ActionDescriptor,
  ActionDescriptorParam,
  type ActionRegistry,
} from "../action_registry";
import type { ActionPayload } from "../action_runner";
import { isNil } from "lodash-es";

export function formDataNormalize(
  actionGroupKey: string,
  actionKey: string,
  formData: FormDataType,
  actionRegistry: ActionRegistry,
): ActionPayload {
  const action = actionRegistry.findAction(actionGroupKey, actionKey);
  const payload = formDataToPayload(action, formData);
  return payload;
}

function formDataToPayload(
  action: ActionDescriptor,
  formData: FormDataType,
): ActionPayload {
  const payload: ActionPayload = {};
  for (const parameter of action.parameters) {
    const name = parameter.name;
    const value = formData[name] ?? null;
    payload[name] = isNil(value) ? null : normalize(parameter, value);
  }
  return payload;
}

function normalize(param: ActionDescriptorParam, value: unknown) {
  if (param.type == "ActorId") return normalizeRef(param, value);
  if (param.type == "AttributeKey") return normalizeKey(param, value);
  if (param.type == "AttributeRef") return normalizeRef(param, value);
  if (param.type == "Boolean") return normalizeBoolean(param, value);
  if (param.type == "EntityAttributeRef") return normalizeRef(param, value);
  if (param.type == "EntityKey") return normalizeKey(param, value);
  if (param.type == "EntityRef") return normalizeRef(param, value);
  if (param.type == "Hashtag") return normalizeString(param, value);
  if (param.type == "LocalizedMarkdown") return normalizeString(param, value);
  if (param.type == "LocalizedText") return normalizeString(param, value);
  if (param.type == "ModelAuthority") return normalizeString(param, value);
  if (param.type == "ModelKey") return normalizeKey(param, value);
  if (param.type == "ModelRef") return normalizeRef(param, value);
  if (param.type == "ModelVersion") return normalizeVersion(param, value);
  if (param.type == "PermissionKey") return normalizeKey(param, value);
  if (param.type == "RelationshipAttributeRef") return normalizeRef(param, value);
  if (param.type == "RelationshipCardinality") return normalizeString(param, value);
  if (param.type == "RelationshipKey") return normalizeKey(param, value);
  if (param.type == "RelationshipRef") return normalizeRef(param, value);
  if (param.type == "RelationshipRoleKey") return normalizeKey(param, value);
  if (param.type == "RelationshipRoleRef") return normalizeRef(param, value);
  if (param.type == "RoleKey") return normalizeKey(param, value);
  if (param.type == "RoleRef") return normalizeRef(param, value);
  if (param.type == "String") return normalizeString(param, value);
  if (param.type == "TagGroupKey") return normalizeKey(param, value);
  if (param.type == "TagGroupRef") return normalizeRef(param, value);
  if (param.type == "TagKey") return normalizeKey(param, value);
  if (param.type == "TagRef") return normalizeRef(param, value);
  if (param.type == "TagScopeRef") return value;
  if (param.type == "TypeKey") return normalizeKey(param, value);
  if (param.type == "TypeRef") return normalizeRef(param, value);
  throw Error("Unsupported type: " + param.type);
}

function normalizeBoolean(param: ActionDescriptorParam, value: unknown) {
  if (value === null && param.optional) return null;
  if (value === undefined && param.optional) return null;
  return value === "true";
}

function normalizeKey(param: ActionDescriptorParam, value: unknown) {
  return normalizeString(param, value);
}

function normalizeRef(param: ActionDescriptorParam, value: unknown) {
  return normalizeString(param, value);
}

function normalizeString(param: ActionDescriptorParam, value: unknown) {
  if (value === null && param.optional) return null;
  if (value === undefined && param.optional) return null;
  if (value === "") return null;
  return value;
}

function normalizeVersion(param: ActionDescriptorParam, value: unknown) {
  return normalizeString(param, value);
}
