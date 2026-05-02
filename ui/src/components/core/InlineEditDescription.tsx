import { useRef, useState } from "react";
import { InlineEditRichTextController } from "./InlineEditRichTextController.tsx";
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
    <InlineEditRichTextController
      editor={
        <MarkdownEditor
          ref={editorRef}
          value={editValue}
          disabled={disabled}
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
    </InlineEditRichTextController>
  );
}

function normalizeMarkdownBeforeSave(value: string): string {
  return value.replace(/<br\s*\/?>/gi, "  \n");
}
