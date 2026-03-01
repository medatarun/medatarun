import {
  type ModelSearchFilter,
  type ModelSearchTagFilter,
  type ModelSearchTagFilterCondition,
} from "@/business/model";
import { useTags } from "@/business/tag";
import {
  Dropdown,
  Option,
  type DropdownProps,
} from "@fluentui/react-components";
import { FilterTagPicker } from "./FilterTagPicker.tsx";
import { useCompactDropdownStyles } from "./Reports.styles.tsx";

export function FilterTagRowEditor({
  filter,
  onChange,
}: {
  filter: ModelSearchTagFilter;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const styles = useCompactDropdownStyles();
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
    <div style={{display: "flex", columnGap: "1em"}}>
      <Dropdown
        className={styles.compactDropdown}
        aria-label="Tag filter condition"
        value={tagConditionLabel(filter.condition)}
        selectedOptions={[filter.condition]}
        onOptionSelect={handleChangeCondition}
      >
        <Option value="anyOf">{tagConditionLabel("anyOf")}</Option>
        <Option value="allOf">{tagConditionLabel("allOf")}</Option>
        <Option value="noneOf">{tagConditionLabel("noneOf")}</Option>
        <Option value="empty">{tagConditionLabel("empty")}</Option>
        <Option value="notEmpty">{tagConditionLabel("notEmpty")}</Option>
      </Dropdown>

      {isTagConditionUsingTags(filter.condition) && (
        <div style={{flex: 1}}>
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
        </div>
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
  if (condition === "anyOf") return "has any of";
  if (condition === "allOf") return "has all of";
  if (condition === "noneOf") return "has none of";
  if (condition === "empty") return "not tagged";
  return "with tags";
}
