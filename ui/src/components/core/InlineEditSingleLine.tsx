import * as React from "react";
import {forwardRef, type PropsWithChildren, useRef, useState} from "react";

import {Input} from "@fluentui/react-components";
import {InlineEditSingleLineLayout, type InlineEditSingleLineLayoutHandle} from "./InlineEditSingleLineLayout.tsx";

export function InlineEditSingleLine({value, children, onChange}: {
  value: string | null | undefined,
  placeholder: string,
  onChange: (value: string) => Promise<unknown>
} & PropsWithChildren) {

  const [editValue, setEditValue] = useState(value ?? "")
  const ref = useRef<HTMLInputElement>(null)
  const layoutRef = useRef<InlineEditSingleLineLayoutHandle>(null)

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

  const handleKeyboardOk = () => {
    layoutRef?.current?.requestOk()
  }

  const handleKeyboardCancel = () => {
    layoutRef?.current?.requestCancel()
  }

  return <InlineEditSingleLineLayout
    ref={layoutRef}
    editor={
      <InputWithTriggers
        ref={ref}
        value={editValue}
        onChange={setEditValue}
        onKeyboardOk={handleKeyboardOk}
        onKeyboardCancel={handleKeyboardCancel}
      />
    }
    onEditStart={handleEditStart}
    onEditStarted={handleEditStarted}
    onEditOK={handeEditOk}
    onEditCancel={handleEditCancel}>{children}</InlineEditSingleLineLayout>
}

const InputWithTriggers = forwardRef<HTMLInputElement, {
  value: string,
  onChange: (value: string) => void,
  onKeyboardOk: () => void,
  onKeyboardCancel: () => void
}>(({onKeyboardOk, onKeyboardCancel, onChange, value}, ref) => {

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault();
      onKeyboardOk()
    }
    if (e.key === "Escape") {
      e.preventDefault();
      onKeyboardCancel()
    }
  }

  return <Input
    ref={ref}
    value={value}
    style={{width: "100%"}}
    onKeyDown={handleKeyDown}
    onChange={(e, data) => onChange(data.value)}/>
})