import { Text, tokens } from "@fluentui/react-components";
import { InputSelect } from "@seij/common-ui";
import { useAppI18n } from "@/services/appI18n.tsx";

export type ComparisonMode = "structural" | "complete";

type ComparisonModeOption = {
  code: ComparisonMode;
  label: string;
};

export function ComparisonModeInput({
  value,
  onChange,
}: {
  value: ComparisonMode;
  onChange: (value: ComparisonMode) => void;
}) {
  const { t } = useAppI18n();

  const options: ComparisonModeOption[] = [
    {
      code: "structural",
      label: t("modelComparePage_modeStructural"),
    },
    {
      code: "complete",
      label: t("modelComparePage_modeComplete"),
    },
  ];

  return (
    <div>
      <Text
        style={{
          display: "block",
          marginBottom: tokens.spacingVerticalXS,
        }}
      >
        {t("modelComparePage_comparisonModeLabel")}
      </Text>
      <InputSelect
        value={value}
        disabled={false}
        options={options}
        onValueChange={(newValue) => {
          if (newValue !== "structural" && newValue !== "complete") return;
          onChange(newValue);
        }}
      />
    </div>
  );
}
