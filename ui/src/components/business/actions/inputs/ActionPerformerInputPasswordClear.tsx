import { Input } from "@fluentui/react-components";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { normalizeValueStringOrEmpty } from "./ActionPerformerInput.utils.ts";

export function ActionPerformerInputPasswordClear({
  inputRef,
  disabled,
  value,
  onValueChange,
}: ActionPerformerInputProps) {
  const valueSafe = normalizeValueStringOrEmpty(value);
  return (
    <Input
      ref={inputRef}
      type="password"
      disabled={disabled}
      value={valueSafe}
      onChange={(_, data) => onValueChange(data.value)}
    />
  );
}
