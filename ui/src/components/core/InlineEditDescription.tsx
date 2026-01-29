import {useRef, useState} from "react";

import {Textarea} from "@fluentui/react-components";
import {InlineEditRichTextLayout} from "./InlineEditRichTextLayout.tsx";
import {Markdown} from "./Markdown.tsx";
import {MissingInformation} from "./MissingInformation.tsx";


export function InlineEditDescription({value, placeholder, onChange}: {
  value: string | null | undefined,
  placeholder: string,
  onChange: (value: string) => Promise<unknown>
}) {

  const [editValue, setEditValue] = useState(value ?? "")
  const ref = useRef<HTMLTextAreaElement>(null)

  const handleEditStart = async () => {
    console.log("handleEditStart")
    setEditValue(value ?? "")
  }

  const handleEditStarted = () => {
    console.log("handleEditStarted", ref)
    ref?.current?.focus()
  }

  const handeEditOk = async () => {
    await onChange(editValue)
  }
  const handleEditCancel = async () => {
    setEditValue("")
  }
  return <InlineEditRichTextLayout
    editor={
      <Textarea ref={ref} value={editValue} rows={20} style={{width: "100%"}} onChange={(e, data) => setEditValue(data.value)}/>
    }
    onEditStart={handleEditStart}
    onEditStarted={handleEditStarted}
    onEditOK={handeEditOk}
    onEditCancel={handleEditCancel}>{value ? <Markdown value={value}/> :
    <MissingInformation>{placeholder}</MissingInformation>}</InlineEditRichTextLayout>
}