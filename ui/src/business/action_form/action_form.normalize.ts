import type { FormDataType } from "./action_form.types.ts";
import {
  ActionDescriptor,
  ActionDescriptorParam,
  type ActionRegistry,
} from "../action_registry";
import type { ActionPayload } from "../action_runner";
import { isNil } from "lodash-es";
import { TypeRegistryInstance } from "@/business/types/TypeRegistry.ts";

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
    const value = formData[name];
    // Normalize null | undefined to null
    const valueNullable = isNil(value) ? null : value;
    const valueNormalized = TypeRegistryInstance.normalize(
      parameter.type,
      parameter,
      valueNullable,
    );
    payload[name] = valueNormalized;
  }
  return payload;
}
