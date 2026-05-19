import {
  ActionDescriptor,
  type ActionKey,
  type ActionRegistry,
} from "../action-registry";
import type { ActionPayload } from "../action-performer";
import { isNil } from "lodash-es";
import {
  TypeRegistry,
  TypeRegistryInstance,
} from "@medatarun/ui/business/types/TypeRegistry.ts";
import type { ActionFormData } from "./action-form-data";

export function formDataNormalize(
  actionRef: ActionKey,
  formData: ActionFormData,
  actionRegistry: ActionRegistry,
  typeRegistry: TypeRegistry,
): ActionPayload {
  const action = actionRegistry.findActionDescriptor(actionRef);
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
