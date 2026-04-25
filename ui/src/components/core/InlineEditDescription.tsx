import { useRef, useState } from "react";
import { InlineEditRichTextLayout } from "./InlineEditRichTextLayout.tsx";
import { Markdown } from "./Markdown.tsx";
import {
  MarkdownEditor,
  type MarkdownEditorHandle,
} from "./MarkdownEditor.tsx";
import { MissingInformation } from "./MissingInformation.tsx";
import { makeStyles, tokens } from "@fluentui/react-components";

const useStyles = makeStyles({
  root: {
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusMedium,
  },
});
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
  const styles = useStyles();

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
    <div className={styles.root}>
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
    </div>
  );
}

function normalizeMarkdownBeforeSave(value: string): string {
  return value.replace(/<br\s*\/?>/gi, "  \n");
}
