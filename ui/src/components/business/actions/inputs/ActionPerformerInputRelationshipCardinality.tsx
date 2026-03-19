import { InputSelect, type InputSelectOption } from "@seij/common-ui";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import {
  adaptPropsValueNullableToValueEmpty,
  normalizeValueStringOrEmpty,
} from "./ActionPerformerInput.utils.ts";

export function ActionPerformerInputRelationshipCardinality(
  props: ActionPerformerInputProps,
) {
  const wrappedProps = adaptPropsValueNullableToValueEmpty(props);
  const valueSafe = normalizeValueStringOrEmpty(wrappedProps.value);
  const options = createRelationshipCardinalityOptions();

  return (
    <InputSelect
      value={valueSafe}
      options={options}
      disabled={wrappedProps.disabled}
      onValueChange={wrappedProps.onValueChange}
    />
  );
}

function createRelationshipCardinalityOptions(): InputSelectOption[] {
  return [
    {
      code: "",
      label: "--",
    },
    {
      code: "zeroOrOne",
      label: "Maybe one",
    },
    {
      code: "many",
      label: "Many",
    },
    {
      code: "one",
      label: "One",
    },
    {
      code: "unknown",
      label: "Unknown number of",
    },
  ];
}
