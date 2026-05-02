import { Input } from "@fluentui/react-components";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { normalizeValueStringOrEmpty } from "./ActionPerformerInput.utils.ts";
import { useImperativeHandle, useRef } from "react";

export function ActionPerformerInputTextBase({
  inputRef,
  disabled,
  value,
  onValueChange,
}: ActionPerformerInputProps) {
  const valueSafe = normalizeValueStringOrEmpty(value);

  const htmlInputRef = useRef<HTMLInputElement>(null);
  useImperativeHandle(inputRef, () => ({
    focus: () => htmlInputRef.current?.focus(),
  }));

  return (
    <Input
      ref={htmlInputRef}
      disabled={disabled}
      value={valueSafe}
      onChange={(_, data) => onValueChange(data.value)}
    />
  );
}
