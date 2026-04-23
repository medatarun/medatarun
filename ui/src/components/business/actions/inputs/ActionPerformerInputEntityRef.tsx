import { Model } from "@/business/model";
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
import { useModel } from "@/components/business/model";

/**
 * Selects an entity in the current model.
 *
 * This works only if the action definition has a modelRef and the request caller specified a modelId.
 */
export function ActionPerformerInputEntityRef(
  props: ActionPerformerInputProps,
) {
  const modelId =
    props.request.ctx.displayedSubject.kind === "resource"
      ? (props.request.ctx.displayedSubject.refs["modelId"] ?? null)
      : null;
  if (!modelId)
    return (
      <ErrorBox
        error={toProblem(
          "Can not create input for entity. Can not find modelId on which operate in action performer request",
        )}
      />
    );
  const wrappedProps = adaptPropsRefIdToRawId(props);
  return (
    <ActionPerformerInputEntityIdSafe {...wrappedProps} modelId={modelId} />
  );
}

function ActionPerformerInputEntityIdSafe({
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
  const options = createEntityOptions(model);
  return (
    <InputSelect
      value={valueSafe}
      options={options}
      onValueChange={onValueChange}
      disabled={disabled}
    />
  );
}

function createEntityOptions(model: Model): InputSelectOption[] {
  return [
    {
      code: "",
      label: "--",
    },
    ...model.findEntityOptions(),
  ];
}
