import {
  type ModelSearchFilter,
  type ModelSearchModelItemField,
  type ModelSearchModelItemFieldCondition,
} from "@/business/model";
import {
  Dropdown,
  Field,
  Input,
  Option,
  tokens,
  type DropdownProps,
} from "@fluentui/react-components";

export function FilterModelItemFieldRowEditor({
  filter,
  onChange,
}: {
  filter: Extract<ModelSearchFilter, { type: "modelItemField" }>;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const handleChangeField: DropdownProps["onOptionSelect"] = (_, data) => {
    const field = data.optionValue as ModelSearchModelItemField | undefined;
    if (field == null) {
      return;
    }
    onChange({
      ...filter,
      field: field,
    });
  };

  const handleChangeCondition: DropdownProps["onOptionSelect"] = (_, data) => {
    const condition =
      data.optionValue as ModelSearchModelItemFieldCondition | undefined;
    if (condition == null) {
      return;
    }
    onChange({
      ...filter,
      condition: condition,
    });
  };

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "180px 180px 1fr",
        gap: tokens.spacingHorizontalM,
        alignItems: "start",
      }}
    >
      <Field label="Model item field">
        <Dropdown
          value={modelItemFieldLabel(filter.field)}
          selectedOptions={[filter.field]}
          onOptionSelect={handleChangeField}
        >
          <Option value="name">Name</Option>
          <Option value="key">Key</Option>
          <Option value="description">Description</Option>
        </Dropdown>
      </Field>
      <Field label="Condition">
        <Dropdown
          value={modelItemFieldConditionLabel(filter.condition)}
          selectedOptions={[filter.condition]}
          onOptionSelect={handleChangeCondition}
        >
          <Option value="contains">Contains</Option>
          <Option value="is">Is</Option>
        </Dropdown>
      </Field>
      <Field label="Value">
        <Input
          value={filter.value}
          onChange={(_, data) =>
            onChange({
              ...filter,
              value: data.value,
            })
          }
        />
      </Field>
    </div>
  );
}

function modelItemFieldLabel(field: ModelSearchModelItemField) {
  if (field === "name") return "Name";
  if (field === "key") return "Key";
  return "Description";
}

function modelItemFieldConditionLabel(
  condition: ModelSearchModelItemFieldCondition,
) {
  if (condition === "contains") return "Contains";
  return "Is";
}
