import { SwitchButton } from "@seij/common-ui";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  type ActionCtx,
  createActionCtx,
  displaySubjectNone,
} from "@/components/business/actions";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { SettingsCogMultipleRegular } from "@fluentui/react-icons";

export function PreferencesPage() {
  const { isDetailLevelTech, toggle } = useDetailLevelContext();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(ActionUILocations.preferences);
  const { t } = useAppI18n();
  const mode = isDetailLevelTech
    ? t("preferencesPage_modeTech")
    : t("preferencesPage_modeBusiness");

  const actionCtxPage: ActionCtx = createActionCtx({
    actionParams: createActionTemplateGeneral(),
    displayedSubject: displaySubjectNone,
  });

  const headerProps: ViewLayoutHeaderProps = {
    title: t("preferencesPage_title"),
    titleIcon: <SettingsCogMultipleRegular />,
    actions: {
      label: t("preferencesPage_actions"),
      itemActions: actions,
      actionCtx: actionCtxPage,
    },
  };
  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
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
    </ViewLayoutContained>
  );
}
