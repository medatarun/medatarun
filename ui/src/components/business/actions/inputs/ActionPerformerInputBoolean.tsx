import type {ActionPerformerInputProps} from "./ActionPerformerInputProps.tsx";
import {InputSelect, type InputSelectOption} from "@seij/common-ui";
import {normalizeValueBooleanOrNull} from "./ActionPerformerInput.utils.ts";

export function ActionPerformerInputBoolean(props: ActionPerformerInputProps) {
  const value = normalizeValueBooleanOrNull(props.value)
  const selectedOption = value === null ? "" : value ? "true" : "false"
  const options = toOptions()
  const handleChangeValue = (v: string) => {
    if (v === "") props.onValueChange(null)
    else if (v === "true") props.onValueChange(true)
    else if (v === "false") props.onValueChange(false)
    else props.onValueChange(null)
  }
  return <InputSelect value={selectedOption} options={options} onValueChange={handleChangeValue}/>
}

function toOptions(): InputSelectOption[] {
  return [{
    code: "",
    label: "--"
  }, {
    code: "true",
    label: "Yes"
  }, {
    code: "false",
    label: "No"
  }]
}