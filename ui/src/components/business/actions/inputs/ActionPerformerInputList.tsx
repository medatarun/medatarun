import { Button, Field, tokens } from "@fluentui/react-components";
import {
  AddRegular,
  ArrowDownRegular,
  ArrowUpRegular,
  DismissRegular,
} from "@fluentui/react-icons";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { useState } from "react";
import {
  ACTION_PERFORMER_INPUT_COMPONENTS_BY_TYPE,
  ACTION_PERFORMER_INPUT_DEFAULT_COMPONENT,
} from "./ActionPerformerInputRegistry.ts";

function normalizeListValue(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

function hasItemValue(value: unknown): boolean {
  if (value === null || value === undefined) return false;
  if (typeof value === "string") return value.trim() !== "";
  return true;
}

/**
 * Edits list parameters by composing an existing single-value input component.
 *
 * The component keeps one draft value for the add row and pushes it into
 * the emitted array when the user clicks "+".
 */
export function ActionPerformerInputList({
  request,
  inputRef,
  value,
  disabled,
  onValueChange,
  inputType,
}: ActionPerformerInputProps<unknown[]> & {
  inputType: string;
}) {
  const values = normalizeListValue(value);
  const [draftValue, setDraftValue] = useState<unknown>(null);
  const InputComponent =
    ACTION_PERFORMER_INPUT_COMPONENTS_BY_TYPE[inputType] ??
    ACTION_PERFORMER_INPUT_DEFAULT_COMPONENT;

  const moveItem = (fromIndex: number, toIndex: number) => {
    if (toIndex < 0 || toIndex >= values.length) return;
    const nextValues = [...values];
    const item = nextValues[fromIndex];
    nextValues[fromIndex] = nextValues[toIndex];
    nextValues[toIndex] = item;
    onValueChange(nextValues);
  };

  const removeItem = (index: number) => {
    const nextValues = values.filter(
      (_, currentIndex) => currentIndex !== index,
    );
    onValueChange(nextValues);
  };

  const updateItem = (index: number, itemValue: unknown) => {
    const nextValues = [...values];
    nextValues[index] = itemValue;
    onValueChange(nextValues);
  };

  const canAdd = !disabled && hasItemValue(draftValue);
  const addDraftValue = () => {
    if (!canAdd) return;
    onValueChange([...values, draftValue]);
    setDraftValue(null);
  };

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        rowGap: tokens.spacingVerticalXXS,
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          columnGap: tokens.spacingHorizontalXXS,
        }}
      >
        <div style={{ flexGrow: 1 }}>
          <Field>
            <InputComponent
              request={request}
              inputRef={inputRef}
              value={draftValue}
              disabled={disabled}
              onValueChange={setDraftValue}
            />
          </Field>
        </div>
        <Button
          aria-label="add list item"
          icon={<AddRegular />}
          onClick={addDraftValue}
          disabled={!canAdd}
        />
      </div>
      {values.map((itemValue, index) => (
        <div
          key={index}
          style={{
            display: "flex",
            alignItems: "center",
            columnGap: tokens.spacingHorizontalXXS,
          }}
        >
          <Button
            aria-label="move item up"
            icon={<ArrowUpRegular />}
            onClick={() => moveItem(index, index - 1)}
            disabled={disabled || index === 0}
          />
          <Button
            aria-label="move item down"
            icon={<ArrowDownRegular />}
            onClick={() => moveItem(index, index + 1)}
            disabled={disabled || index === values.length - 1}
          />
          <div style={{ flexGrow: 1 }}>
            <Field>
              <InputComponent
                request={request}
                inputRef={undefined}
                value={itemValue}
                disabled={disabled}
                onValueChange={(nextValue) => updateItem(index, nextValue)}
              />
            </Field>
          </div>
          <Button
            aria-label="remove item"
            icon={<DismissRegular />}
            onClick={() => removeItem(index)}
            disabled={disabled}
          />
        </div>
      ))}
    </div>
  );
}
