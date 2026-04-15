import { invalid, type ValidationResult } from "@seij/common-validation";
import { type AppMessageKey, appT } from "@/services/appI18n.tsx";
import type { FormDataType, FormFieldType } from "./action_form.types.ts";
import { TypeRegistryInstance } from "@/business/types/TypeRegistry.ts";

const t = (key: AppMessageKey, values?: Record<string, unknown>) =>
  appT(key, values);

export function validateForm({
  formData,
  formFields,
}: {
  formData: FormDataType;
  formFields: FormFieldType[];
}): Map<string, ValidationResult> {
  const validationResults: Map<string, ValidationResult> = new Map();
  for (const formField of formFields) {
    let result;
    try {
      result = TypeRegistryInstance.validate(
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
