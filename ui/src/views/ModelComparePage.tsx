import { useModelCompare, useModelSummaries, type ModelSummaryDto } from "@/business/model";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { Button, InputCombobox } from "@seij/common-ui";
import { Text, tokens } from "@fluentui/react-components";
import { useEffect, useState } from "react";
import { useAppI18n } from "@/services/appI18n.tsx";

type CompareScopeOption = {
  code: "structural" | "complete";
  label: string;
};

export function ModelComparePage() {
  const { data: modelSummaries = [] } = useModelSummaries();
  const compare = useModelCompare();
  const { t } = useAppI18n();
  const [leftModelId, setLeftModelId] = useState("");
  const [rightModelId, setRightModelId] = useState("");
  const [scope, setScope] = useState<"structural" | "complete">("structural");
  const [scopeQuery, setScopeQuery] = useState(t("modelComparePage_modeStructural"));

  const scopeOptions: CompareScopeOption[] = [
    {
      code: "structural",
      label: t("modelComparePage_modeStructural"),
    },
    {
      code: "complete",
      label: t("modelComparePage_modeComplete"),
    },
  ];

  const handleCompare = async () => {
    await compare.mutateAsync({
      leftModelId: leftModelId,
      rightModelId: rightModelId,
      scope: scope,
    });
  };

  const canCompare =
    leftModelId.length > 0 &&
    rightModelId.length > 0 &&
    leftModelId !== rightModelId &&
    !compare.isPending;

  return (
    <ViewLayoutContained
      title={
        <ViewTitle>
          <span>{t("modelComparePage_title")}</span>
        </ViewTitle>
      }
    >
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr",
          gap: tokens.spacingVerticalM,
          padding: tokens.spacingHorizontalM,
        }}
      >
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "2fr 2fr 1.2fr auto",
            gap: tokens.spacingHorizontalM,
            alignItems: "end",
          }}
        >
          <InputModel
            label={t("modelComparePage_leftModelLabel")}
            modelSummaries={modelSummaries}
            value={leftModelId}
            onChange={setLeftModelId}
            placeholder={t("modelComparePage_selectModelPlaceholder")}
            noOptionsMessage={t("modelComparePage_modelsNoOptions")}
          />

          <InputModel
            label={t("modelComparePage_rightModelLabel")}
            modelSummaries={modelSummaries}
            value={rightModelId}
            onChange={setRightModelId}
            placeholder={t("modelComparePage_selectModelPlaceholder")}
            noOptionsMessage={t("modelComparePage_modelsNoOptions")}
          />

          <div>
            <Text
              style={{
                display: "block",
                marginBottom: tokens.spacingVerticalXS,
              }}
            >
              {t("modelComparePage_comparisonModeLabel")}
            </Text>
            <InputCombobox
              searchQuery={scopeQuery}
              disabled={false}
              options={scopeOptions}
              placeholder={t("modelComparePage_selectComparisonModePlaceholder")}
              noOptionsMessage={t("modelComparePage_comparisonModeNoOptions")}
              onValueChangeQuery={setScopeQuery}
              onValueChange={(newValue) => {
                const matched = scopeOptions.find(
                  (option) => option.code === newValue,
                );
                if (!matched) return;
                setScope(matched.code);
                setScopeQuery(matched.label);
              }}
            />
          </div>

          <div>
            <Button disabled={!canCompare} onClick={handleCompare}>
              {t("modelComparePage_compareButton")}
            </Button>
          </div>
        </div>

        <pre
          style={{
            marginTop: tokens.spacingVerticalM,
            whiteSpace: "pre-wrap",
            wordBreak: "break-word",
            background: "var(--colorNeutralBackground3)",
            padding: tokens.spacingHorizontalM,
            borderRadius: tokens.borderRadiusMedium,
          }}
        >
          {compare.data
            ? JSON.stringify(compare.data, null, 2)
            : t("modelComparePage_resultPlaceholder")}
        </pre>
      </div>
    </ViewLayoutContained>
  );
}

function InputModel({
  label,
  modelSummaries,
  value,
  onChange,
  placeholder,
  noOptionsMessage,
}: {
  label: string;
  modelSummaries: ModelSummaryDto[];
  value: string;
  onChange: (value: string) => void;
  placeholder: string;
  noOptionsMessage: string;
}) {
  const options = modelSummaries.map((summary) => {
    const baseLabel = summary.name ?? summary.key;
    return {
      code: summary.id,
      label: baseLabel + " (" + summary.key + ")",
    };
  });
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    const matched = options.find((option) => option.code === value);
    if (!matched) return;
    setSearchQuery(matched.label);
  }, [options, value]);

  return (
    <div>
      <Text
        style={{
          display: "block",
          marginBottom: tokens.spacingVerticalXS,
        }}
      >
        {label}
      </Text>
      <InputCombobox
        searchQuery={searchQuery}
        disabled={false}
        options={options}
        placeholder={placeholder}
        noOptionsMessage={noOptionsMessage}
        onValueChangeQuery={setSearchQuery}
        onValueChange={(newValue) => {
          onChange(newValue);
          const matched = options.find((option) => option.code === newValue);
          if (!matched) return;
          setSearchQuery(matched.label);
        }}
      />
    </div>
  );
}
