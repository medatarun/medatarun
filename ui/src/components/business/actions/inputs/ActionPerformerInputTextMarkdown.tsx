import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { normalizeValueStringOrEmpty } from "./ActionPerformerInput.utils.ts";
import { MarkdownEditor } from "@/components/core/MarkdownEditor.tsx";

export function ActionPerformerInputTextMarkdown({
  inputRef,
  disabled,
  value,
  onValueChange,
}: ActionPerformerInputProps) {
  const valueSafe = normalizeValueStringOrEmpty(value);
  return (
    <div
      style={{
        width: "100%",
        minWidth: 0,
        maxWidth: "100%",
        boxSizing: "border-box",
      }}
    >
      <MarkdownEditor
        ref={inputRef}
        value={valueSafe}
        onChange={(data) => onValueChange(data)}
        disabled={disabled}
      />
    </div>
  );
}
