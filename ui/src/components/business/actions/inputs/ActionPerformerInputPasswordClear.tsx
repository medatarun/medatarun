import { Input } from "@fluentui/react-components";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { normalizeValueStringOrEmpty } from "./ActionPerformerInput.utils.ts";
import { useImperativeHandle, useRef } from "react";

export function ActionPerformerInputPasswordClear({
  inputRef,
  disabled,
  value,
  onValueChange,
}: ActionPerformerInputProps) {
  const valueSafe = normalizeValueStringOrEmpty(value);

  const passwordInputRef = useRef<HTMLInputElement>(null);
  useImperativeHandle(inputRef, () => ({
    focus: () => passwordInputRef.current?.focus(),
  }));

  return (
    <Input
      ref={passwordInputRef}
      type="password"
      disabled={disabled}
      value={valueSafe}
      onChange={(_, data) => onValueChange(data.value)}
    />
  );
}
