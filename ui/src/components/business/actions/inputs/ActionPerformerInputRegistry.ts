import type { FunctionComponent } from "react";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { ActionPerformerInputTextBase } from "./ActionPerformerInputTextBase.tsx";

export type ActionPerformerInputComponent =
  FunctionComponent<ActionPerformerInputProps>;

export type ActionPerformerInputComponentsByType = Record<
  string,
  ActionPerformerInputComponent
>;

/**
 * Resolves the input component for an action parameter scalar type.
 */
export class ActionPerformerInputRegistry {
  private readonly componentsByType: ActionPerformerInputComponentsByType;

  constructor(componentsByType: ActionPerformerInputComponentsByType) {
    this.componentsByType = componentsByType;
  }

  findComponentByTypeOrDefault(type: string): ActionPerformerInputComponent {
    return this.componentsByType[type] ?? ActionPerformerInputTextBase;
  }
}
