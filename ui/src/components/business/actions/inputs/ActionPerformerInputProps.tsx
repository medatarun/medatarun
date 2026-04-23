import type { Ref } from "react";
import type { ActionRequest } from "@/business/action-performer";

export interface ActionPerformerInputProps<T = unknown> {
  request: ActionRequest;
  inputRef: Ref<HTMLInputElement> | undefined;
  value: T | null;
  disabled: boolean;
  onValueChange: (value: T) => void;
}
