import { InputCombobox } from "@seij/common-ui";
import { type PropsWithChildren, useState } from "react";
import { InlineEditSingleLineController } from "./InlineEditSingleLineController.tsx";

export interface InlineEditComboboxOption {
  code: string;
  label: string;
}

export function InlineEditCombobox({
  value,
  disabled = false,
  options,
  placeholder,
  noOptionsMessage,
  children,
  onChange,
}: {
  value: string;
  disabled?: boolean;
  options: InlineEditComboboxOption[];
  placeholder: string;
  noOptionsMessage?: string;
  onChange: (value: string) => Promise<unknown>;
} & PropsWithChildren) {
  const [editValue, setEditValue] = useState(value);
  const [searchQuery, setSearchQuery] = useState("");

  const handleEditStart = async () => {
    setEditValue(value);
    const currentOption = options.find((option) => option.code === value);
    setSearchQuery(currentOption?.label ?? value);
  };

  const handleEditOk = async () => {
    await onChange(editValue);
  };

  const handleEditCancel = async () => {
    setEditValue(value);
    const currentOption = options.find((option) => option.code === value);
    setSearchQuery(currentOption?.label ?? value);
  };

  const handleValueChange = (newValue: string) => {
    setEditValue(newValue);
    const option = options.find((item) => item.code === newValue);
    setSearchQuery(option?.label ?? newValue);
  };

  return (
    <InlineEditSingleLineController
      disabled={disabled}
      editor={({ commit, cancel, pending }) => (
        <div>
          <InputCombobox
            searchQuery={searchQuery}
            placeholder={placeholder}
            disabled={pending}
            options={options}
            noOptionsMessage={noOptionsMessage}
            onValueChangeQuery={setSearchQuery}
            onValueChange={handleValueChange}
            onEnter={commit}
          />
        </div>
      )}
      onEditStart={handleEditStart}
      onEditOK={handleEditOk}
      onEditCancel={handleEditCancel}
    >
      {children}
    </InlineEditSingleLineController>
  );
}
