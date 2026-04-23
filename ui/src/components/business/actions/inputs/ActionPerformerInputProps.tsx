import type { Ref } from "react";
import type { ActionPerformerRequest } from "@/business/action-performer";

export interface ActionPerformerInputProps<T = unknown> {
  request: ActionPerformerRequest;
  inputRef: Ref<HTMLInputElement> | undefined;
  value: T | null;
  disabled: boolean;
  onValueChange: (value: T) => void;
}
