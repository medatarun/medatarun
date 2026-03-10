import { useModelCompare, useModelSummaries, type ModelSummaryDto } from "@/business/model";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { Button, InputCombobox } from "@seij/common-ui";
import { Text, tokens } from "@fluentui/react-components";
import { useEffect, useState } from "react";

type CompareScopeOption = {
  code: "structural" | "complete";
  label: string;
};

const scopeOptions: CompareScopeOption[] = [
  { code: "structural", label: "Structurel" },
  { code: "complete", label: "Complet" },
];

export function ModelComparePage() {
  const { data: modelSummaries = [] } = useModelSummaries();
  const compare = useModelCompare();
  const [leftModelId, setLeftModelId] = useState("");
  const [rightModelId, setRightModelId] = useState("");
  const [scope, setScope] = useState<"structural" | "complete">("structural");
  const [scopeQuery, setScopeQuery] = useState("Structurel");

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
          <span>Model Compare</span>
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
        <InputModel
          label="Modèle gauche"
          modelSummaries={modelSummaries}
          value={leftModelId}
          onChange={setLeftModelId}
        />

        <InputModel
          label="Modèle droite"
          modelSummaries={modelSummaries}
          value={rightModelId}
          onChange={setRightModelId}
        />

        <div>
          <Text
            style={{
              display: "block",
              marginBottom: tokens.spacingVerticalXS,
            }}
          >
            Scope
          </Text>
          <InputCombobox
            searchQuery={scopeQuery}
            disabled={false}
            options={scopeOptions}
            placeholder="Choisir un scope"
            noOptionsMessage="Aucun scope"
            onValueChangeQuery={setScopeQuery}
            onValueChange={(newValue) => {
              const matched = scopeOptions.find((option) => option.code === newValue);
              if (!matched) return;
              setScope(matched.code);
              setScopeQuery(matched.label);
            }}
          />
        </div>

        <div>
          <Button disabled={!canCompare} onClick={handleCompare}>
            Comparer
          </Button>
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
            : "// Résultat JSON du compare"}
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
}: {
  label: string;
  modelSummaries: ModelSummaryDto[];
  value: string;
  onChange: (value: string) => void;
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
        placeholder="Choisir un modèle"
        noOptionsMessage="Aucun modèle trouvé"
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
