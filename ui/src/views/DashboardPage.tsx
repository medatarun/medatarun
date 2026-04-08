import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { tokens } from "@fluentui/react-components";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { ContainedHumanReadable } from "@/components/layout/Contained.tsx";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

export function DashboardPage() {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(ActionUILocations.global);
  const { t } = useAppI18n();
  return (
    <ViewLayoutContained
      title={
        <div>
          <ViewTitle eyebrow={t("dashboardPage_eyebrow")}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div>{t("dashboardPage_title")}</div>
              <div>
                <ActionMenuButton
                  label={t("dashboardPage_actions")}
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
      <ContainedHumanReadable>
        <div>
          <MissingInformation>
            {t("dashboardPage_placeholder")}
          </MissingInformation>
        </div>
      </ContainedHumanReadable>
    </ViewLayoutContained>
  );
}
