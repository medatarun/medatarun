import * as React from "react";
import {forwardRef, type PropsWithChildren, useRef, useState} from "react";

import {
  Avatar,
  Tag,
  TagPicker,
  TagPickerControl,
  TagPickerGroup,
  TagPickerInput,
  TagPickerList,
  TagPickerOption,
  type TagPickerProps
} from "@fluentui/react-components";
import {InlineEditSingleLineLayout} from "./InlineEditSingleLineLayout.tsx";
import {TagList, useTagScopedList, type TagScopeRef} from "../../business";

export function InlineEditTags({value, scope, children, onChange}: {
  value: string[],
  scope: TagScopeRef,
  onChange: (value: string[]) => Promise<unknown>
} & PropsWithChildren) {
  const [values, setValues] = useState(value)
  const ref = useRef<HTMLInputElement>(null)
  const {tagList} = useTagScopedList(scope)

  const handleEditStart = async () => {
    setValues(value)
  }

  const handleEditStarted = () => {
    ref?.current?.focus()
  }

  const handeEditOk = async () => {
    await onChange(values)
  }
  const handleEditCancel = async () => {
    setValues(value)
  }


  return <InlineEditSingleLineLayout
    editor={({commit, cancel, pending}) =>
      <InputWithKeys
        ref={ref}
        disabled={pending}
        tagList={tagList}
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
  tagList: TagList,
  onCommit: () => void,
  onCancel: () => void,
  onChange: (value: string[]) => void
}
const InputWithKeys = forwardRef<HTMLInputElement, InputWithKeysProps>(
  ({values, disabled, tagList, onChange, onCommit, onCancel}, ref) => {

    const [inputValue, setInputValue] = useState("")
    const [isComposing, setIsComposing] = useState(false)
    const options = tagList.search(inputValue, values)

    const onOptionSelect: TagPickerProps["onOptionSelect"] = (_, data) => {
      onChange(data.selectedOptions);
      setInputValue("")
    };
    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
      setInputValue(event.currentTarget.value);
    };
    // Some keyboards used to compose characters in languages such as Japanese or Chinese
    // emit Enter before the text is fully validated. We ignore that Enter so the picker
    // does not commit the edit or select an option too early.
    const handleKeyDown = (e: React.KeyboardEvent) => {
      if (e.key === "Enter") {
        if (isComposing) return
        if (inputValue.trim() === "") {
          e.preventDefault();
          onCommit()
        }
      }
      if (e.key === "Escape") {
        e.preventDefault();
        onCancel()
      }
    }

    return <div style={{position:"relative"}}>
      <TagPicker onOptionSelect={onOptionSelect} selectedOptions={values}>
      <TagPickerControl >
      <TagPickerGroup aria-label="Selected tags">
        {values.map((option, index) => (
          <Tag
            key={index}
            shape="rounded"
            media={<Avatar aria-hidden name={tagList.formatLabel(option)} color="colorful" />}
            value={option}
          >
            {tagList.formatLabel(option)}
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
      <TagPickerList>
        {options.map(option =>
          <TagPickerOption key={option.id} value={option.id} text={tagList.formatLabel(option.id)}>
            {tagList.formatLabel(option.id)}
          </TagPickerOption>
        )}
      </TagPickerList>
    </TagPicker></div>
  }
)
