import {
  type ModelSearchFilter,
  type ModelSearchTextFilter,
} from "@/business/model";
import {Input} from "@fluentui/react-components";

export function FilterTextRowEditor({
  filter,
  onChange,
}: {
  filter: ModelSearchTextFilter;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  return (
    <Input
      aria-label="Text contains value"
      placeholder="Text contains..."
      value={filter.value}
      onChange={(_, data) =>
        onChange({
          ...filter,
          value: data.value,
        })
      }
    />
  );
}
