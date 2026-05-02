import type { TypeRegistry } from "@/business/types/TypeRegistry.ts";
import { ActionRegistry } from "@/business/action_registry";
import type { ActionFormData } from "@/business/action-form/action-form-data.ts";
import type { ActionPayload } from "@/business/action-performer";
import { formDataNormalize } from "@/business/action-form/action-form-normalize.ts";
import type { ActionFormFieldDescription } from "@/business/action-form/action-form-field-description.ts";
import { validateForm } from "@/business/action-form/action-form-validate.ts";

export class ActionFormService {
  private readonly typeRegistry: TypeRegistry;
  private readonly actionRegistry: ActionRegistry;
  constructor(typeRegistry: TypeRegistry, actionRegistry: ActionRegistry) {
    this.typeRegistry = typeRegistry;
    this.actionRegistry = actionRegistry;
  }

  formDataNormalize(
    actionGroupKey: string,
    actionKey: string,
    formData: ActionFormData,
  ): ActionPayload {
    return formDataNormalize(
      actionGroupKey,
      actionKey,
      formData,
      this.actionRegistry,
      this.typeRegistry,
    );
  }

  validateForm(
    formData: ActionFormData,
    formFields: ActionFormFieldDescription[],
  ) {
    return validateForm({
      formData,
      formFields,
      typeRegistry: this.typeRegistry,
    });
  }
}
