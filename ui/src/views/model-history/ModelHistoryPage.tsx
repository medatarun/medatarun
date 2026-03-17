import { Text } from "@fluentui/react-components";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";

export function ModelHistoryPage() {
  const { t } = useAppI18n();

  return (
    <ViewLayoutContained
      title={<ViewTitle eyebrow="">{t("modelHistoryPage_title")}</ViewTitle>}
    >
      <Text>Model history todo</Text>
    </ViewLayoutContained>
  );
}
