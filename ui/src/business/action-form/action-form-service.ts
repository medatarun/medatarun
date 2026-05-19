import type { TypeRegistry } from "@medatarun/ui/business/types/TypeRegistry.ts";
import { type ActionKey, ActionRegistry } from "../action-registry";
import type { ActionFormData } from "@medatarun/ui/business/action-form/action-form-data.ts";
import type { ActionPayload } from "@medatarun/ui/business/action-performer";
import { formDataNormalize } from "@medatarun/ui/business/action-form/action-form-normalize.ts";
import type { ActionFormFieldDescription } from "@medatarun/ui/business/action-form/action-form-field-description.ts";
import { validateForm } from "@medatarun/ui/business/action-form/action-form-validate.ts";

export class ActionFormService {
  private readonly typeRegistry: TypeRegistry;
  private readonly actionRegistry: ActionRegistry;
  constructor(typeRegistry: TypeRegistry, actionRegistry: ActionRegistry) {
    this.typeRegistry = typeRegistry;
    this.actionRegistry = actionRegistry;
  }

  formDataNormalize(
    actionKey: ActionKey,
    formData: ActionFormData,
  ): ActionPayload {
    return formDataNormalize(
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
