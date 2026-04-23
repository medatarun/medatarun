import { ModelCard } from "@/components/business/model/ModelCard.tsx";
import { useActionRegistry } from "@/business/action_registry";
import { type ModelSummaryDto, useModelSummaries } from "@/business/model";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { InfoLabel, tokens } from "@fluentui/react-components";
import { ModelIcon } from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { createActionCtxVoid } from "@/business/action-performer";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { SectionCards } from "@/components/layout/SectionCards.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import type { ReactNode } from "react";

export function ModelListPage({
  onClickModel,
}: {
  onClickModel: (modelId: string) => void;
}) {
  const { data = [] } = useModelSummaries();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActionDescriptors([
    "model_create",
    "import",
  ]);
  const { t } = useAppI18n();
  const canonicalModels = data.filter(
    (model) => model.authority === "canonical",
  );
  const systemModels = data.filter((model) => model.authority !== "canonical");

  const actionCtxPage = createActionCtxVoid();

  const headerProps: ViewLayoutHeaderProps = {
    title: t("modelListPage_title"),
    titleIcon: <ModelIcon authority={undefined} />,
    actions: {
      label: t("modelListPage_actions"),
      itemActions: actions,
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      title={<ViewLayoutHeader {...headerProps} />}
      contained={true}
      scrollable={true}
    >
      {data.length == 0 ? t("modelListPage_empty") : null}
      {canonicalModels.length > 0 && (
        <ModelsSection
          title={t("modelListPage_canonicalTitle")}
          titleIcon={<ModelIcon authority={"canonical"} />}
          titleInfo={t("modelListPage_canonicalInfo")}
          models={canonicalModels}
          onClickModel={onClickModel}
        />
      )}
      {systemModels.length > 0 && (
        <ModelsSection
          title={t("modelListPage_systemTitle")}
          titleIcon={<ModelIcon authority={"system"} />}
          titleInfo={t("modelListPage_systemInfo")}
          models={systemModels}
          onClickModel={onClickModel}
        />
      )}
    </ViewLayoutContained>
  );
}

function ModelsSection({
  title,
  titleInfo,
  titleIcon,
  models,
  onClickModel,
}: {
  title: string;
  titleInfo: string;
  titleIcon: ReactNode;
  models: ModelSummaryDto[];
  onClickModel: (modelId: string) => void;
}) {
  return (
    <>
      <SectionTitle icon={titleIcon}>
        <span
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: tokens.spacingHorizontalXS,
          }}
        >
          {title}
          <InfoLabel info={titleInfo}></InfoLabel>
        </span>
      </SectionTitle>
      <SectionCards>
        <div
          style={{
            display: "flex",
            justifyContent: "left",
            columnGap: tokens.spacingVerticalM,
            rowGap: tokens.spacingVerticalM,
            flexWrap: "wrap",
            marginTop: tokens.spacingVerticalM,
          }}
        >
          {models.map((model) => (
            <ModelCard key={model.id} model={model} onClick={onClickModel} />
          ))}
        </div>
      </SectionCards>
    </>
  );
}
