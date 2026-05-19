import { ModelCard } from "@medatarun/ui/components/business/model/ModelCard.tsx";
import { type ModelListItemDto } from "@medatarun/ui/business/model";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";
import { InfoLabel, tokens } from "@fluentui/react-components";
import { ModelIcon } from "@medatarun/ui/components/business/model/model.icons.tsx";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import { createActionCtxVoid } from "@medatarun/ui/business/action-performer";
import { SectionTitle } from "@medatarun/ui/components/layout/SectionTitle.tsx";
import { SectionCards } from "@medatarun/ui/components/layout/SectionCards.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader.tsx";
import type { ReactNode } from "react";
import { useActionRegistry } from "@medatarun/ui/components/business/actions";
import { useModelList } from "@medatarun/ui/components/business/model";
import { AdminDangerPanel } from "@medatarun/ui/components/business/onboarding/AdminDangerPanel.tsx";
import { ModelLimitedPermissionsPanel } from "@medatarun/ui/components/business/onboarding/ModelLimitedPermissionsPanel.tsx";
import { ListEmpty } from "@medatarun/ui/components/core/ListEmpty.tsx";
import { useSecurityContext } from "@medatarun/ui/components/business/security";

export function ModelListPage({
  onClickModel,
}: {
  onClickModel: (modelId: string) => void;
}) {
  const { data: resp } = useModelList();
  const data = resp?.items ?? [];
  const actionRegistry = useActionRegistry();
  const security = useSecurityContext();
  const actions = actionRegistry.findActionDescriptors([
    "models/model_create",
    "models/import",
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

  const canRead = security.canExecuteAction("models/model_list");
  const displayEmptyMesage = canRead && data.length === 0;

  return (
    <ViewLayoutContained
      title={<ViewLayoutHeader {...headerProps} />}
      contained={true}
      scrollable={true}
    >
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          marginTop: tokens.spacingVerticalM,
          rowGap: tokens.spacingVerticalXXXL,
        }}
      >
        <AdminDangerPanel />
        <ModelLimitedPermissionsPanel />
        <ListEmpty display={displayEmptyMesage}>
          {t("modelListPage_empty")}
        </ListEmpty>
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
      </div>
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
  models: ModelListItemDto[];
  onClickModel: (modelId: string) => void;
}) {
  return (
    <div>
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
    </div>
  );
}
