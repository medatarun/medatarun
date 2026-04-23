import {
  type ModelSearchFilter,
  type ModelSearchTagFilter,
  type ModelSearchTagFilterCondition,
} from "@/business/model";
import { useTags } from "@/components/business/tag";
import {
  Dropdown,
  type DropdownProps,
  Option,
} from "@fluentui/react-components";
import { type AppMessageKey, useAppI18n } from "@/services/appI18n.tsx";
import { FilterTagPicker } from "./FilterTagPicker.tsx";
import { useCompactDropdownStyles } from "./Reports.styles.tsx";

export function FilterTagRowEditor({
  filter,
  onChange,
}: {
  filter: ModelSearchTagFilter;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const { t } = useAppI18n();
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
    <div style={{ display: "flex", columnGap: "1em" }}>
      <Dropdown
        className={styles.compactDropdown}
        aria-label={t("modelReportsFilterTag_conditionAriaLabel")}
        value={tagConditionLabel(filter.condition, t)}
        selectedOptions={[filter.condition]}
        onOptionSelect={handleChangeCondition}
      >
        <Option value="anyOf">{tagConditionLabel("anyOf", t)}</Option>
        <Option value="allOf">{tagConditionLabel("allOf", t)}</Option>
        <Option value="noneOf">{tagConditionLabel("noneOf", t)}</Option>
        <Option value="empty">{tagConditionLabel("empty", t)}</Option>
        <Option value="notEmpty">{tagConditionLabel("notEmpty", t)}</Option>
      </Dropdown>

      {isTagConditionUsingTags(filter.condition) && (
        <div style={{ flex: 1 }}>
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
    condition === "anyOf" || condition === "allOf" || condition === "noneOf"
  );
}

function tagConditionLabel(
  condition: ModelSearchTagFilterCondition,
  t: (key: AppMessageKey, values?: Record<string, unknown>) => string,
) {
  if (condition === "anyOf") return t("modelReportsFilterTag_conditionAnyOf");
  if (condition === "allOf") return t("modelReportsFilterTag_conditionAllOf");
  if (condition === "noneOf") return t("modelReportsFilterTag_conditionNoneOf");
  if (condition === "empty") return t("modelReportsFilterTag_conditionEmpty");
  return t("modelReportsFilterTag_conditionNotEmpty");
}
