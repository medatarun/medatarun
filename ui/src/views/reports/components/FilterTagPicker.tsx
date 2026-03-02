import { type Tags } from "@/business/tag";
import {
  Avatar,
  Tag,
  TagPicker,
  TagPickerControl,
  TagPickerGroup,
  TagPickerInput,
  TagPickerList,
  TagPickerOption,
  type TagPickerProps,
} from "@fluentui/react-components";
import { useState } from "react";
import { useAppI18n } from "@/services/appI18n.tsx";

export function FilterTagPicker({
  disabled,
  selectedTagIds,
  tags,
  onChange,
}: {
  disabled: boolean;
  selectedTagIds: string[];
  tags: Tags;
  onChange: (tagIds: string[]) => void;
}) {
  const { t } = useAppI18n();
  const [inputValue, setInputValue] = useState("");
  const [open, setOpen] = useState(false);
  const options = tags.search(inputValue, selectedTagIds);

  const handleOptionSelect: TagPickerProps["onOptionSelect"] = (_, data) => {
    onChange(data.selectedOptions);
    setInputValue("");
    setOpen(false);
  };

  return (
    <TagPicker
      open={open}
      selectedOptions={selectedTagIds}
      onOpenChange={(_, data) => setOpen(data.open)}
      onOptionSelect={handleOptionSelect}
    >
      <TagPickerControl>
        <TagPickerGroup aria-label={t("modelReportsTagPicker_selectedAriaLabel")}>
          {selectedTagIds.map((tagId) => (
            <Tag
              key={tagId}
              shape="rounded"
              media={
                <Avatar
                  aria-hidden
                  name={tags.formatLabel(tagId)}
                  color="colorful"
                />
              }
              value={tagId}
            >
              {tags.formatLabel(tagId)}
            </Tag>
          ))}
        </TagPickerGroup>
        <TagPickerInput
          value={inputValue}
          aria-label={t("modelReportsTagPicker_searchAriaLabel")}
          disabled={disabled}
          onFocus={() => setOpen(true)}
          onChange={(event) => {
            setInputValue(event.currentTarget.value);
            setOpen(true);
          }}
        />
      </TagPickerControl>
      <TagPickerList>
        {options.length === 0 && (
          <div
            style={{
              padding: "8px 12px",
              color: "var(--colorNeutralForeground3)",
            }}
          >
            {t("modelReportsTagPicker_empty")}
          </div>
        )}
        {options.map((option) => (
          <TagPickerOption
            key={option.id}
            value={option.id}
            text={tags.formatLabel(option.id)}
          >
            {tags.formatLabel(option.id)}
          </TagPickerOption>
        ))}
      </TagPickerList>
    </TagPicker>
  );
}
