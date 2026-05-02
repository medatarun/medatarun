import { invalid, type ValidationResult } from "@seij/common-validation";
import { type AppMessageKey, appT } from "@/services/appI18n.tsx";
import type { ActionFormFieldDescription } from "./action-form-field-description.ts";
import { TypeRegistry } from "@/business/types/TypeRegistry.ts";
import type { ActionFormData } from "./action-form-data";

const t = (key: AppMessageKey, values?: Record<string, unknown>) =>
  appT(key, values);

export function validateForm({
  formData,
  formFields,
  typeRegistry,
}: {
  formData: ActionFormData;
  formFields: ActionFormFieldDescription[];
  typeRegistry: TypeRegistry;
}): Map<string, ValidationResult> {
  const validationResults: Map<string, ValidationResult> = new Map();
  for (const formField of formFields) {
    let result;
    try {
      result = typeRegistry.validate(
        formField.type,
        formField,
        formData[formField.key],
      );
    } catch (e: unknown) {
      result = invalid(
        t("formValidation_unsupportedType", { type: formField.type }),
      );
    }
    validationResults.set(formField.key, result);
  }
  return validationResults;
}
