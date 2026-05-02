import {
  type ForwardedRef,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
} from "react";
import { Editor } from "@toast-ui/react-editor";
import "@toast-ui/editor/dist/toastui-editor.css";

export interface MarkdownEditorHandle {
  focus: () => void;
}

export interface MarkdownEditorProps {
  value: string;
  disabled: boolean;
  onChange: (value: string) => void;
}

export const MarkdownEditor = forwardRef(function MarkdownEditor(
  props: MarkdownEditorProps,
  ref: ForwardedRef<MarkdownEditorHandle>,
) {
  const editorRef = useRef<Editor>(null);

  useImperativeHandle(
    ref,
    () => ({
      focus: () => {
        editorRef.current?.getInstance().focus();
      },
    }),
    [],
  );

  useEffect(() => {
    const editorInstance = editorRef.current?.getInstance();
    if (!editorInstance) {
      return;
    }

    // Toast UI editor is internally stateful. Keep it aligned with external value.
    if (editorInstance.getMarkdown() !== props.value) {
      editorInstance.setMarkdown(props.value, false);
    }
  }, [props.value]);

  const handleChange = () => {
    if (props.disabled) return;
    const nextValue = editorRef.current?.getInstance().getMarkdown();
    if (nextValue != null) {
      props.onChange(nextValue);
    }
  };
  return (
    <Editor
      ref={editorRef}
      height="360px"
      initialEditType="wysiwyg"
      hideModeSwitch={true}
      initialValue={props.value}
      usageStatistics={false}
      toolbarItems={[
        ["heading", "bold", "italic", "strike"],
        ["hr", "quote"],
        ["ul", "ol", "task", "indent", "outdent"],
        ["link"],
      ]}
      onChange={handleChange}
    />
  );
});
