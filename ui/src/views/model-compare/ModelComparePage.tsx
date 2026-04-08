import {
  type ModelChangeEventWithVersionDto,
  type ModelSummaryDto,
  useModelCompare,
  useModelHistoryVersions,
  useModelSummaries,
} from "@/business/model";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import {
  ContainedFixed,
  ContainedHeader,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  type ComparisonMode,
  ComparisonModeInput,
} from "@/views/model-compare/ComparisonModeInput.tsx";
import { ModelCompareDiffView } from "@/views/model-compare/ModelCompareDiffView.tsx";
import { ModelHistoryVersionInput } from "@/views/model-history/components/ModelHistoryVersionInput.tsx";
import { Text, tokens } from "@fluentui/react-components";
import { ArrowBidirectionalLeftRightRegular } from "@fluentui/react-icons";
import { Button, InputCombobox } from "@seij/common-ui";
import { useEffect, useState } from "react";

export function ModelComparePage() {
  const { data: modelSummaries = [] } = useModelSummaries();
  const compare = useModelCompare();
  const { t } = useAppI18n();
  const [leftModelId, setLeftModelId] = useState("");
  const [leftModelVersion, setLeftModelVersion] = useState<string | null>(null);
  const [rightModelId, setRightModelId] = useState("");
  const [rightModelVersion, setRightModelVersion] = useState<string | null>(
    null,
  );
  const [comparisonMode, setComparisonMode] =
    useState<ComparisonMode>("structural");
  const { data: leftVersionsDto } = useModelHistoryVersions(leftModelId);
  const { data: rightVersionsDto } = useModelHistoryVersions(rightModelId);

  useEffect(() => {
    setLeftModelVersion(null);
  }, [leftModelId]);

  useEffect(() => {
    setRightModelVersion(null);
  }, [rightModelId]);

  const handleCompare = async () => {
    await compare.mutateAsync({
      leftModelId: leftModelId,
      leftModelVersion: leftModelVersion,
      rightModelId: rightModelId,
      rightModelVersion: rightModelVersion,
      scope: comparisonMode,
    });
  };

  const canCompare =
    leftModelId.length > 0 && rightModelId.length > 0 && !compare.isPending;

  const headerProps: ViewLayoutHeaderProps = {
    title: t("modelComparePage_title"),
    titleIcon: <ArrowBidirectionalLeftRightRegular />,
  };

  return (
    <ViewLayoutContained
      contained={false}
      scrollable={false}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <ContainedMixedScrolling>
        <ContainedFixed>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "1.6fr 1fr 1.6fr 1fr 1.2fr auto",
              gap: tokens.spacingHorizontalM,
              alignItems: "end",
              marginLeft: tokens.spacingHorizontalM,
              marginRight: tokens.spacingHorizontalM,
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

            <InputVersion
              label={t("modelHistoryPage_versionsTitle")}
              versions={leftVersionsDto?.items ?? []}
              value={leftModelVersion}
              onChange={setLeftModelVersion}
            />

            <InputModel
              label={t("modelComparePage_rightModelLabel")}
              modelSummaries={modelSummaries}
              value={rightModelId}
              onChange={setRightModelId}
              placeholder={t("modelComparePage_selectModelPlaceholder")}
              noOptionsMessage={t("modelComparePage_modelsNoOptions")}
            />

            <InputVersion
              label={t("modelHistoryPage_versionsTitle")}
              versions={rightVersionsDto?.items ?? []}
              value={rightModelVersion}
              onChange={setRightModelVersion}
            />

            <ComparisonModeInput
              value={comparisonMode}
              onChange={setComparisonMode}
            />

            <div>
              <Button disabled={!canCompare} onClick={handleCompare}>
                {t("modelComparePage_compareButton")}
              </Button>
            </div>
          </div>
        </ContainedFixed>
        <ContainedScrollable>
          <div
            style={{
              marginLeft: tokens.spacingHorizontalM,
              marginRight: tokens.spacingHorizontalM,
            }}
          >
            {compare.data ? (
              <ModelCompareDiffView diff={compare.data} />
            ) : (
              <MissingInformation>
                {t("modelComparePage_emptyState")}
              </MissingInformation>
            )}
          </div>
        </ContainedScrollable>
      </ContainedMixedScrolling>
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

function InputVersion({
  label,
  versions,
  value,
  onChange,
}: {
  label: string;
  versions: ModelChangeEventWithVersionDto[];
  value: string | null;
  onChange: (value: string | null) => void;
}) {
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
      <ModelHistoryVersionInput
        versions={versions}
        value={value}
        onChange={onChange}
      />
    </div>
  );
}
