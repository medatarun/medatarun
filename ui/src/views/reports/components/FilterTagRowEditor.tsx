import {
  type ModelSearchFilter,
  type ModelSearchTagFilter,
  type ModelSearchTagFilterCondition,
} from "@/business/model";
import { useTags } from "@/business/tag";
import {
  Dropdown,
  Field,
  Option,
  tokens,
  type DropdownProps,
} from "@fluentui/react-components";
import { FilterTagPicker } from "./FilterTagPicker.tsx";

export function FilterTagRowEditor({
  filter,
  onChange,
}: {
  filter: ModelSearchTagFilter;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const { tags, isPending } = useTags();

  const handleChangeCondition: DropdownProps["onOptionSelect"] = (_, data) => {
    const condition = data.optionValue;
    if (
      condition !== "anyOf" &&
      condition !== "allOf" &&
      condition !== "noneOf" &&
      condition !== "empty" &&
      condition !== "notEmpty"
    ) {
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
        gridTemplateColumns: isTagConditionUsingTags(filter.condition)
          ? "220px 1fr"
          : "220px",
        gap: tokens.spacingHorizontalM,
        alignItems: "start",
      }}
    >
      <Field label="Tag filter">
        <Dropdown
          value={tagConditionLabel(filter.condition)}
          selectedOptions={[filter.condition]}
          onOptionSelect={handleChangeCondition}
        >
          <Option value="anyOf">Any of</Option>
          <Option value="allOf">All of</Option>
          <Option value="noneOf">None of</Option>
          <Option value="empty">Empty</Option>
          <Option value="notEmpty">Not empty</Option>
        </Dropdown>
      </Field>

      {isTagConditionUsingTags(filter.condition) && (
        <Field label="Tags">
          <FilterTagPicker
            disabled={isPending}
            selectedTagIds={filter.tagIds}
            tags={tags}
            onChange={(tagIds) =>
              onChange({
                ...filter,
                tagIds: tagIds,
              })
            }
          />
        </Field>
      )}
    </div>
  );
}

function isTagConditionUsingTags(condition: ModelSearchTagFilterCondition) {
  return (
    condition === "anyOf" ||
    condition === "allOf" ||
    condition === "noneOf"
  );
}

function tagConditionLabel(condition: ModelSearchTagFilterCondition) {
  if (condition === "anyOf") return "Any of";
  if (condition === "allOf") return "All of";
  if (condition === "noneOf") return "None of";
  if (condition === "empty") return "Empty";
  return "Not empty";
}
