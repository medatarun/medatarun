import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { normalizeValueStringOrEmpty } from "./ActionPerformerInput.utils.ts";
import { MarkdownEditor } from "@/components/core/MarkdownEditor.tsx";
import { useImperativeHandle, useRef } from "react";
import type { RichTextEditorRef } from "@seij/common-ui-richtext";

export function ActionPerformerInputTextMarkdown({
  inputRef,
  disabled,
  value,
  onValueChange,
}: ActionPerformerInputProps) {
  const valueSafe = normalizeValueStringOrEmpty(value);
  const richTextEditorRef = useRef<RichTextEditorRef>(null);
  useImperativeHandle(inputRef, () => ({
    focus: () => richTextEditorRef.current?.focus(),
  }));
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
        ref={richTextEditorRef}
        value={valueSafe}
        onChange={(data) => onValueChange(data)}
        disabled={disabled}
      />
    </div>
  );
}
