import { ModelCard } from "@/components/business/model/ModelCard.tsx";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { type ModelSummaryDto, useModelSummaries } from "@/business/model";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { tokens } from "@fluentui/react-components";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { ModelIcon } from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";

export function ModelsPage({
  onClickModel,
}: {
  onClickModel: (modelId: string) => void;
}) {
  const { data = [] } = useModelSummaries();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(ActionUILocations.models);
  const { t } = useAppI18n();

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
          {data.map((model: ModelSummaryDto) => (
            <ModelCard key={model.id} model={model} onClick={onClickModel} />
          ))}
        </div>
      </div>
    </ViewLayoutContained>
  );
}
