import { SwitchButton } from "@seij/common-ui";
import { useDetailLevelContext } from "@/components/business/detail-level";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { SettingsCogMultipleRegular } from "@fluentui/react-icons";

export function PreferencesPage() {
  const { isDetailLevelTech, toggle } = useDetailLevelContext();
  const { t } = useAppI18n();
  const mode = isDetailLevelTech
    ? t("preferencesPage_modeTech")
    : t("preferencesPage_modeBusiness");

  const headerProps: ViewLayoutHeaderProps = {
    title: t("preferencesPage_title"),
    titleIcon: <SettingsCogMultipleRegular />,
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
