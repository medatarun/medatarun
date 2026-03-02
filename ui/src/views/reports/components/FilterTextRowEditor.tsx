import {
  type ModelSearchFilter,
  type ModelSearchTextFilter,
} from "@/business/model";
import {Input} from "@fluentui/react-components";
import { useAppI18n } from "@/services/appI18n.tsx";

export function FilterTextRowEditor({
  filter,
  onChange,
}: {
  filter: ModelSearchTextFilter;
  onChange: (filter: ModelSearchFilter) => void;
}) {
  const { t } = useAppI18n();
  return (
    <Input
      aria-label={t("modelReportsFilterText_valueAriaLabel")}
      placeholder={t("modelReportsFilterText_placeholder")}
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
