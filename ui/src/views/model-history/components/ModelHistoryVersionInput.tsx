import type {ModelChangeEventWithVersionDto} from "@/business/model";
import {InputSelect} from "@seij/common-ui";
import {useAppI18n} from "@/services/appI18n.tsx";

const LAST_CHANGES_OPTION = "__last_changes__";

export function ModelHistoryVersionInput(
  {
    versions,
    value,
    onChange,
  }: {
    versions: ModelChangeEventWithVersionDto[];
    value: string | null;
    onChange: (value: string | null) => void;
  }) {

  const { t } = useAppI18n();
  const lastChangesLabel = t("modelHistoryPage_lastChanges")
  const options = toOptions(versions, lastChangesLabel);
  const selectedCode = value ?? LAST_CHANGES_OPTION;

  const handleValueChange = (nextValue: string) => {
    onChange(nextValue === LAST_CHANGES_OPTION ? null : nextValue);
  };

  return (
    <InputSelect
      options={options}
      disabled={false}
      value={selectedCode}
      onValueChange={handleValueChange}
    />
  );
}

function toOptions(
  versions: ModelChangeEventWithVersionDto[],
  lastChangesLabel: string
): { code: string, label: string }[] {
  return [
    {
      code: LAST_CHANGES_OPTION,
      label: lastChangesLabel,
    },
    ...versions.map((version) => ({
      code: version.modelVersion,
      label: version.modelVersion,
    })),
  ]
}
