import { ActionDescriptor, type ActionRegistry } from "../action_registry";
import type { ActionPayload } from "../action-performer";
import { isNil } from "lodash-es";
import {
  TypeRegistry,
  TypeRegistryInstance,
} from "@/business/types/TypeRegistry.ts";
import type { ActionFormData } from "./action-form-data";

export function formDataNormalize(
  actionGroupKey: string,
  actionKey: string,
  formData: ActionFormData,
  actionRegistry: ActionRegistry,
  typeRegistry: TypeRegistry,
): ActionPayload {
  const action = actionRegistry.findActionByGroupKeyAndActionKey(
    actionGroupKey,
    actionKey,
  );
  const payload = formDataToPayload(action, formData, typeRegistry);
  return payload;
}

function formDataToPayload(
  action: ActionDescriptor,
  formData: ActionFormData,
  typeRegistry: TypeRegistry,
): ActionPayload {
  const payload: ActionPayload = {};
  for (const parameter of action.parameters) {
    const name = parameter.name;
    const value = formData[name];
    // Normalize null | undefined to null
    const valueNullable = isNil(value) ? null : value;
    const valueNormalized = typeRegistry.normalize(
      parameter.type,
      parameter,
      valueNullable,
    );
    payload[name] = valueNormalized;
  }
  return payload;
}
