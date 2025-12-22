import {Combobox, useComboboxFilter} from "@fluentui/react-components";


export interface InputComboboxProps<T> {
  /**
   * The value of the text input.
   */
  searchQuery: string;
  /**
   * The placeholder text to display when the input is empty.
   */
  placeholder: string;
  /**
   * Indicates whether the input is disabled.
   */
  disabled: boolean;
  /**
   * The list of options to display in the dropdown.
   */
  options: T[];
  /**
   * The message to display when no option is found.
   */
  noOptionsMessage?: string;
  /**
   * The function to call when the input value changes.
   */
  onValueChangeQuery: (value: string) => void;
  /**
   * The function to call when the user selects an option from the dropdown.
   */
  onValueChange: (value: string) => void;
}

/**
 * A text input combobox with a dropdown list of options.
 * @returns A textbox component with a dropdown list of options.
 * @example
 * <InputCombobox
 *   searchQuery={searchQuery}
 *   placeholder="Select an option"
 *   disabled={false}
 *   options={[{ code: "1", label: "Option 1" }, { code: "2", label: "Option 2" }]}
 *   noOptionsMessage="No options found"
 *   onValueChangeQuery={(value) => setSearchQuery(value)}
 *   onValueChange={(value) => setSelectedOption(value)}
 * />
 */
export function InputCombobox<T extends { code: string; label: string }>(
  {
    searchQuery,
    placeholder,
    disabled,
    options,
    noOptionsMessage,
    onValueChangeQuery,
    onValueChange,
  }: InputComboboxProps<T>) {

  const noresult = "No result found";
  return (
    <Combobox
      style={{width: "100%"}}
      value={searchQuery}
      onOptionSelect={(_, data) => {
        onValueChangeQuery(data.optionText ?? "");
        onValueChange(data.optionValue ?? "");
      }}
      onChange={(ev) => onValueChangeQuery(ev.target.value)}
      clearable
      placeholder={placeholder}
      disabled={disabled}
      freeform={true}
    >
      {useComboboxFilter(
        searchQuery,
        options.map((o) => ({children: o.label, value: o.code})),
        {
          optionToReactKey: (option) => option.value,
          optionToText: (option) => option.children,
          noOptionsMessage: noOptionsMessage ?? noresult,
        },
      )}
    </Combobox>
  );
}
