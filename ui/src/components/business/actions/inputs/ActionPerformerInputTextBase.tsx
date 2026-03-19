import {Input} from "@fluentui/react-components";
import type {ActionPerformerInputProps} from "./ActionPerformerInputProps.tsx";
import {normalizeValueStringOrEmpty} from "./ActionPerformerInput.utils.ts";

export function ActionPerformerInputTextBase({inputRef, disabled, value, onValueChange}: ActionPerformerInputProps) {
  const valueSafe = normalizeValueStringOrEmpty(value)
  return (
    <Input
      ref={inputRef}
      disabled={disabled}
      value={valueSafe}
      onChange={(_, data) => onValueChange(data.value)}
    />
  );
}