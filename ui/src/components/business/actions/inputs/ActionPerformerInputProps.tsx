import type { Ref } from "react";
import type { ActionRequest } from "@/business/action-performer";

export interface ActionPerformerInputElement {
  focus: () => void;
}
export interface ActionPerformerInputProps<T = unknown> {
  request: ActionRequest;
  inputRef: Ref<ActionPerformerInputElement> | undefined;
  value: T | null;
  disabled: boolean;
  onValueChange: (value: T) => void;
}
