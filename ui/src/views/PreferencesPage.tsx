import { SwitchButton } from "@seij/common-ui";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { tokens } from "@fluentui/react-components";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { ContainedHumanReadable } from "@/components/layout/Contained.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {createActionTemplateGeneral, createDisplayedSubjectModel} from "@/components/business/model/model.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import {displaySubjectNone} from "@/components/business/actions/ActionPerformer.tsx";

export function PreferencesPage() {
  const { isDetailLevelTech, toggle } = useDetailLevelContext();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(ActionUILocations.preferences);
  const { t } = useAppI18n();
  const mode = isDetailLevelTech
    ? t("preferencesPage_modeTech")
    : t("preferencesPage_modeBusiness");
  return (
    <ViewLayoutContained
      title={
        <div>
          <ViewTitle eyebrow="">
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div>{t("preferencesPage_title")}</div>
              <div>
                <ActionMenuButton
                  label={t("preferencesPage_actions")}
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
          <div>
            <p>{t("preferencesPage_currentMode", { mode })}</p>
            <p>{t("preferencesPage_modeDescription")}</p>
          </div>
          <SwitchButton
            value={isDetailLevelTech}
            onValueChange={toggle}
            labelTrue={t("preferencesPage_switchToBusinessMode")}
            labelFalse={t("preferencesPage_switchToTechnicalMode")}
          />
        </div>
      </ContainedHumanReadable>
    </ViewLayoutContained>
  );
}
