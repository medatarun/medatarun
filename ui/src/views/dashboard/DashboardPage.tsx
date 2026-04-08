import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { ContainedHumanReadable } from "@/components/layout/Contained.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader";
import { useAppI18n } from "@/services/appI18n.tsx";
import { ChartMultipleRegular } from "@fluentui/react-icons";

export function DashboardPage() {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(ActionUILocations.global);
  const { t } = useAppI18n();
  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("dashboardPage_eyebrow"),
    title: t("dashboardPage_title"),
    titleIcon: <ChartMultipleRegular />,
    actions: {
      label: t("dashboardPage_actions"),
      itemActions: actions,
      actionParams: createActionTemplateGeneral(),
      displayedSubject: displaySubjectNone,
    },
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
