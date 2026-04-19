import { useRef, useState } from "react";
import { InlineEditRichTextLayout } from "./InlineEditRichTextLayout.tsx";
import { Markdown } from "./Markdown.tsx";
import {
  MarkdownEditor,
  type MarkdownEditorHandle,
} from "./MarkdownEditor.tsx";
import { MissingInformation } from "./MissingInformation.tsx";

export function InlineEditDescription({
  value,
  placeholder,
  onChange,
  disabled = false,
}: {
  value: string | null | undefined;
  placeholder: string;
  onChange: (value: string) => Promise<unknown>;
  disabled?: boolean;
}) {
  const editorRef = useRef<MarkdownEditorHandle>(null);
  const [editValue, setEditValue] = useState(value ?? "");

  const handleEditStart = async () => {
    setEditValue(value ?? "");
  };

  const handleEditStarted = () => {
    editorRef.current?.focus();
  };

  const handleEditOk = async () => {
    await onChange(normalizeMarkdownBeforeSave(editValue));
  };

  const handleEditCancel = async () => {
    setEditValue(value ?? "");
  };

  return (
    <InlineEditRichTextLayout
      editor={
        <MarkdownEditor
          ref={editorRef}
          value={editValue}
          onChange={setEditValue}
        />
      }
      disabled={disabled}
      onEditStart={handleEditStart}
      onEditStarted={handleEditStarted}
      onEditOK={handleEditOk}
      onEditCancel={handleEditCancel}
    >
      {value ? (
        <Markdown value={value} />
      ) : (
        <MissingInformation>{placeholder}</MissingInformation>
      )}
    </InlineEditRichTextLayout>
  );
}

function normalizeMarkdownBeforeSave(value: string): string {
  return value.replace(/<br\s*\/?>/gi, "  \n");
}
