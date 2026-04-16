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
 * Selects an attribute from the currently displayed entity.
 *
 * This works only if the request displayed subject provides both modelId and entityId refs.
 */
export function ActionPerformerInputEntityAttributeRef(
  props: ActionPerformerInputProps,
) {
  const modelId =
    props.request.ctx.displayedSubject.kind === "resource"
      ? (props.request.ctx.displayedSubject.refs["modelId"] ?? null)
      : null;
  const entityId =
    props.request.ctx.displayedSubject.kind === "resource"
      ? (props.request.ctx.displayedSubject.refs["entityId"] ?? null)
      : null;
  if (!modelId || !entityId)
    return (
      <ErrorBox
        error={toProblem(
          "Can not create input for entity attribute. Can not find modelId/entityId on which operate in action performer request",
        )}
      />
    );
  const wrappedProps = adaptPropsRefIdToRawId(props);
  return (
    <ActionPerformerInputEntityAttributeIdSafe
      {...wrappedProps}
      modelId={modelId}
      entityId={entityId}
    />
  );
}

function ActionPerformerInputEntityAttributeIdSafe({
  modelId,
  entityId,
  value,
  onValueChange,
  disabled,
}: ActionPerformerInputProps<string> & {
  modelId: string;
  entityId: string;
}) {
  const valueSafe = normalizeValueStringOrEmpty(value);
  const { data: modelDto } = useModel(modelId);
  if (modelDto == null) return <Loader loading={true} />;
  const model = new Model(modelDto);
  const options = createEntityAttributeOptions(model, entityId);
  return (
    <InputSelect
      value={valueSafe}
      options={options}
      onValueChange={onValueChange}
      disabled={disabled}
    />
  );
}

function createEntityAttributeOptions(
  model: Model,
  entityId: string,
): InputSelectOption[] {
  return [
    {
      code: "",
      label: "--",
    },
    ...model.findEntityAttributeOptions(entityId),
  ];
}
