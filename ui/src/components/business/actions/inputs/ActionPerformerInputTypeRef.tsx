import { Model, useModel } from "@/business/model";
import {
  ErrorBox,
  InputSelect,
  type InputSelectOption,
  Loader,
} from "@seij/common-ui";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { toProblem } from "@seij/common-types";
import {
  adaptPropsRefIdToRawId,
  normalizeValueStringOrEmpty,
} from "./ActionPerformerInput.utils.ts";

/**
 * Selects a type in the model types.
 *
 * This works only if the action definition has a modelRef and the request caller specified a modelId
 */
export function ActionPerformerInputTypeRef(props: ActionPerformerInputProps) {
  const modelId =
    props.request.displayedSubject.kind === "resource"
      ? (props.request.displayedSubject.refs["modelId"] ?? null)
      : null;
  if (!modelId)
    return (
      <ErrorBox
        error={toProblem(
          "Can not create input for type. Can not find modelId on which operate in action performer request",
        )}
      />
    );
  const wrappedProps = adaptPropsRefIdToRawId(props);
  return <ActionPerformerInputTypeIdSafe {...wrappedProps} modelId={modelId} />;
}

function ActionPerformerInputTypeIdSafe({
  modelId,
  value,
  onValueChange,
  disabled,
}: ActionPerformerInputProps<string> & {
  modelId: string;
}) {
  const valueSafe = normalizeValueStringOrEmpty(value);
  const { data: modelDto } = useModel(modelId);
  if (modelDto == null) return <Loader loading={true} />;
  const model = new Model(modelDto);
  const options = createTypeOptions(model);
  return (
    <InputSelect
      value={valueSafe}
      options={options}
      onValueChange={onValueChange}
      disabled={disabled}
    />
  );
}

function createTypeOptions(model: Model): InputSelectOption[] {
  return [
    {
      code: "",
      label: "--",
    },
    ...model.findTypeOptions(),
  ];
}
