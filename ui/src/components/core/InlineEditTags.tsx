import * as React from "react";
import {forwardRef, type PropsWithChildren, useRef, useState} from "react";

import {
  Avatar,
  Tag,
  TagPicker,
  TagPickerControl,
  TagPickerGroup,
  TagPickerInput,
  type TagPickerProps
} from "@fluentui/react-components";
import {InlineEditSingleLineLayout} from "./InlineEditSingleLineLayout.tsx";

export function InlineEditTags({value, children, onChange}: {
  value: string[],
  onChange: (value: string[]) => Promise<unknown>
} & PropsWithChildren) {

  const [values, setValues] = useState(value)
  const ref = useRef<HTMLInputElement>(null)

  const handleEditStart = async () => {
    console.log("handleEditStart")
    setValues(value ?? "")
  }

  const handleEditStarted = () => {
    console.log("handleEditStarted", ref)
    ref?.current?.focus()
  }

  const handeEditOk = async () => {
    await onChange(values)
  }
  const handleEditCancel = async () => {
    setValues([])
  }


  return <InlineEditSingleLineLayout
    editor={({commit, cancel, pending}) =>
      <InputWithKeys
        ref={ref}
        disabled={pending}
        values={values}
        onChange={setValues}
        onCommit={commit}
        onCancel={cancel}/>
    }
    onEditStart={handleEditStart}
    onEditStarted={handleEditStarted}
    onEditOK={handeEditOk}
    onEditCancel={handleEditCancel}>{children}</InlineEditSingleLineLayout>
}

type InputWithKeysProps = {
  values: string[],
  disabled: boolean,
  onCommit: () => void,
  onCancel: () => void,
  onChange: (value: string[]) => void
}
const InputWithKeys = forwardRef<HTMLInputElement, InputWithKeysProps>(
  ({values, disabled, onChange, onCommit, onCancel}, ref) => {

    const [inputValue, setInputValue] = useState("")
    const [isComposing, setIsComposing] = useState(false)

    const onOptionSelect: TagPickerProps["onOptionSelect"] = (_, data) => {
      onChange(data.selectedOptions);
    };
    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
      setInputValue(event.currentTarget.value);
    };
    const handleKeyDown = (e: React.KeyboardEvent) => {
      if (e.key === "Enter") {
        if (isComposing) return // Asian inputs
        e.preventDefault();
        if (inputValue) {
          setInputValue("")
          const next = values.includes(inputValue) ? values : [...values, inputValue]
          onChange(next)
        } else {
          onCommit()
        }



      }
      if (e.key === "Escape") {
        e.preventDefault();
        onCancel()
      }
    }

    return <div style={{position:"relative"}}><TagPicker noPopover onOptionSelect={onOptionSelect} selectedOptions={values}>
      <TagPickerControl >
      <TagPickerGroup aria-label="Selected tags">
        {values.map((option, index) => (
          <Tag
            key={index}
            shape="rounded"
            media={<Avatar aria-hidden name={option} color="colorful" />}
            value={option}
          >
            {option}
          </Tag>
        ))}
      </TagPickerGroup>
      <TagPickerInput
        ref={ref}
        value={inputValue}
        style={{width: "100%"}}
        aria-label="Add Tags"
        disabled={disabled}
        onCompositionStart={() => setIsComposing(true)}
        onCompositionEnd={() => setIsComposing(false)}
        onKeyDown={handleKeyDown}
        onChange={handleChange}/>
      </TagPickerControl>
    </TagPicker></div>
  }
)


