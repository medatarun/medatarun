import { type ForwardedRef, forwardRef } from "react";
import {
  type RichTextEditorClassNames,
  RichTextEditorMarkdown,
  type RichTextEditorRef,
} from "@seij/common-ui-richtext";
import { Logger } from "tslog";

export type MarkdownEditorHandle = RichTextEditorRef;

export interface MarkdownEditorProps {
  value: string;
  disabled: boolean;
  classNames?: RichTextEditorClassNames;
  onChange: (value: string) => void;
}
const logger = new Logger();
export const MarkdownEditor = forwardRef(function MarkdownEditor(
  props: MarkdownEditorProps,
  ref: ForwardedRef<MarkdownEditorHandle>,
) {
  const handleChange = (value: string) => {
    logger.debug("handleChange", value);
    props.onChange(value);
  };
  return (
    <RichTextEditorMarkdown
      ref={ref}
      value={props.value}
      disabled={props.disabled}
      classNames={props.classNames}
      onChange={handleChange}
    />
  );
});
