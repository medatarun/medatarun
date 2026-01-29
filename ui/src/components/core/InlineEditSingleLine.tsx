import * as React from "react";
import {forwardRef, type PropsWithChildren, useRef, useState} from "react";

import {Input} from "@fluentui/react-components";
import {InlineEditSingleLineLayout} from "./InlineEditSingleLineLayout.tsx";

export function InlineEditSingleLine({value, children, onChange}: {
  value: string | null | undefined,
  placeholder: string,
  onChange: (value: string) => Promise<unknown>
} & PropsWithChildren) {

  const [editValue, setEditValue] = useState(value ?? "")
  const ref = useRef<HTMLInputElement>(null)

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


  return <InlineEditSingleLineLayout
    editor={({commit, cancel, pending}) =>
      <InputWithKeys
        ref={ref}
        disabled={pending}
        editValue={editValue}
        onChange={setEditValue}
        onCommit={commit}
        onCancel={cancel}/>
    }
    onEditStart={handleEditStart}
    onEditStarted={handleEditStarted}
    onEditOK={handeEditOk}
    onEditCancel={handleEditCancel}>{children}</InlineEditSingleLineLayout>
}

type InputWithKeysProps = {
  editValue: string,
  disabled: boolean,
  onCommit: () => void,
  onCancel: () => void,
  onChange: (value: string) => void
}
const InputWithKeys = forwardRef<HTMLInputElement, InputWithKeysProps>(
  ({editValue, disabled, onChange, onCommit, onCancel}, ref) => {
    const [isComposing, setIsComposing] = useState(false)
    return <Input
      ref={ref}
      value={editValue}
      style={{width: "100%"}}
      disabled={disabled}
      onCompositionStart={() => setIsComposing(true)}
      onCompositionEnd={() => setIsComposing(false)}
      onKeyDown={(e) => {
        if (e.key === "Enter") {
          if (isComposing) return // Asian inputs
          e.preventDefault();
          onCommit()
        }
        if (e.key === "Escape") {
          e.preventDefault();
          onCancel()
        }
      }}
      onChange={(_, data) => onChange(data.value)}/>
  }
)


