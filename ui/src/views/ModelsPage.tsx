import { ModelCard } from "@/components/business/model/ModelCard.tsx";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  Model,
  type ModelSummaryDto,
  useModelSummaries,
} from "@/business/model";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { InfoLabel, tokens } from "@fluentui/react-components";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { ModelIcon } from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { SectionCards } from "@/components/layout/SectionCards.tsx";

export function ModelsPage({
  onClickModel,
}: {
  onClickModel: (modelId: string) => void;
}) {
  const { data = [] } = useModelSummaries();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(ActionUILocations.models);
  const { t } = useAppI18n();
  const canonicalModels = data.filter(
    (model) => model.authority === "canonical",
  );
  const systemModels = data.filter((model) => model.authority !== "canonical");

  return (
    <ViewLayoutContained
      title={
        <div>
          <ViewTitle>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div>
                <span>
                  <ModelIcon />
                </span>{" "}
                {t("modelsPage_title")}{" "}
              </div>
              <div>
                <ActionMenuButton
                  label={t("modelsPage_actions")}
                  itemActions={actions}
                  actionParams={createActionTemplateGeneral()}
                  displayedSubject={displaySubjectNone}
                />
              </div>
            </div>
          </ViewTitle>
        </div>
      }
    >
      <div
        style={{
          paddingLeft: tokens.spacingHorizontalM,
          paddingRight: tokens.spacingHorizontalM,
        }}
      >
        {data.length == 0 ? t("modelsPage_empty") : null}
        {canonicalModels.length > 0 && (
          <ModelsSection
            title={`${Model.authorityEmoji("canonical")} ${t("modelsPage_canonicalTitle")}`}
            titleInfo={t("modelsPage_canonicalInfo")}
            models={canonicalModels}
            onClickModel={onClickModel}
          />
        )}
        {systemModels.length > 0 && (
          <ModelsSection
            title={`${Model.authorityEmoji("system")} ${t("modelsPage_systemTitle")}`}
            titleInfo={t("modelsPage_systemInfo")}
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
  models,
  onClickModel,
}: {
  title: string;
  titleInfo: string;
  models: ModelSummaryDto[];
  onClickModel: (modelId: string) => void;
}) {
  return (
    <>
      <SectionTitle
        icon={undefined}
        actionParams={createActionTemplateGeneral()}
        displayedSubject={displaySubjectNone}
        location={ActionUILocations.none}
      >
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
            justifyContent: "center",
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
