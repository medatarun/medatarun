import * as React from "react";
import {forwardRef, type PropsWithChildren, useEffect, useRef, useState} from "react";

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
import {useActionPerformer} from "../business/ActionPerformerHook.tsx";

const CREATE_OPTION_PREFIX = "__create__:"

export function InlineEditTags({value, scope, children, onChange}: {
  value: string[],
  scope: TagScopeRef,
  onChange: (value: string[]) => Promise<unknown>
} & PropsWithChildren) {
  const [values, setValues] = useState(value)
  const ref = useRef<HTMLInputElement>(null)
  const {tagList} = useTagScopedList(scope)
  const {performAction, state} = useActionPerformer()
  const [requestedCreatedTagKey, setRequestedCreatedTagKey] = useState<string | null>(null)
  const [waitingCreatedTagResolution, setWaitingCreatedTagResolution] = useState(false)
  const previousStateKindRef = useRef(state.kind)

  useEffect(() => {
    const previousStateKind = previousStateKindRef.current
    previousStateKindRef.current = state.kind

    if (requestedCreatedTagKey == null) {
      return
    }
    const actionFinished =
      previousStateKind === "running"
      && state.kind === "done"
      && state.request.actionGroupKey === "tag"
      && state.request.actionKey === "tag_free_create"

    if (actionFinished) {
      setWaitingCreatedTagResolution(true)
    }

    if (!waitingCreatedTagResolution && !actionFinished) {
      return
    }

    const createdTag = tagList.findByScopeAndKey(scope, requestedCreatedTagKey)
    if (!createdTag) {
      return
    }
    setValues(previousValues => previousValues.includes(createdTag.id) ? previousValues : [...previousValues, createdTag.id])
    setRequestedCreatedTagKey(null)
    setWaitingCreatedTagResolution(false)
  }, [requestedCreatedTagKey, scope, state, tagList, waitingCreatedTagResolution])

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
    setRequestedCreatedTagKey(null)
    setWaitingCreatedTagResolution(false)
  }

  const handleCreateTag = (key: string) => {
    setRequestedCreatedTagKey(key)
    setWaitingCreatedTagResolution(false)
    performAction({
      actionGroupKey: "tag",
      actionKey: "tag_free_create",
      params: {
        scopeRef: {value: scope, readonly: true},
        key: {value: key, readonly: false},
      }
    })
  }


  return <InlineEditSingleLineLayout
    editor={({commit, cancel, pending}) =>
      <InputWithKeys
        ref={ref}
        disabled={pending}
        scope={scope}
        tagList={tagList}
        values={values}
        onCreateTag={handleCreateTag}
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
  scope: TagScopeRef,
  tagList: TagList,
  onCommit: () => void,
  onCancel: () => void,
  onCreateTag: (key: string) => void,
  onChange: (value: string[]) => void
}
const InputWithKeys = forwardRef<HTMLInputElement, InputWithKeysProps>(
  ({values, disabled, scope, tagList, onChange, onCommit, onCancel, onCreateTag}, ref) => {

    const [inputValue, setInputValue] = useState("")
    const [isComposing, setIsComposing] = useState(false)
    const options = tagList.search(inputValue, values)
    const trimmedInputValue = inputValue.trim()
    const canCreateTag = scope.type !== "global"
      && trimmedInputValue !== ""
      && tagList.findByScopeAndKey(scope, trimmedInputValue) == null

    const onOptionSelect: TagPickerProps["onOptionSelect"] = (_, data) => {
      const selectedOption = data.value
      if (typeof selectedOption === "string" && selectedOption.startsWith(CREATE_OPTION_PREFIX)) {
        onCreateTag(selectedOption.slice(CREATE_OPTION_PREFIX.length))
        setInputValue("")
        return
      }
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
        {canCreateTag &&
          <TagPickerOption
            key={CREATE_OPTION_PREFIX + trimmedInputValue}
            value={CREATE_OPTION_PREFIX + trimmedInputValue}
            text={`Create "${trimmedInputValue}"`}>
            Create "{trimmedInputValue}"
          </TagPickerOption>
        }
        {options.map(option =>
          <TagPickerOption key={option.id} value={option.id} text={tagList.formatLabel(option.id)}>
            {tagList.formatLabel(option.id)}
          </TagPickerOption>
        )}
      </TagPickerList>
    </TagPicker></div>
  }
)
