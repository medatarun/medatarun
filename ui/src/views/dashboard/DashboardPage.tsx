import { MissingInformation } from "@medatarun/ui/components/core/MissingInformation.tsx";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import { ChartMultipleRegular } from "@fluentui/react-icons";

export function DashboardPage() {
  const { t } = useAppI18n();
  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("dashboardPage_eyebrow"),
    title: t("dashboardPage_title"),
    titleIcon: <ChartMultipleRegular />,
  };
  return (
    <ViewLayoutContained
      scrollable={true}
      contained={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <div>
        <MissingInformation>
          {t("dashboardPage_placeholder")}
        </MissingInformation>
      </div>
    </ViewLayoutContained>
  );
}
