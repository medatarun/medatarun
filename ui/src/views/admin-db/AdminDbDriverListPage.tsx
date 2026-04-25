import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { Caption2, Text, tokens } from "@fluentui/react-components";
import { useAppI18n } from "@/services/appI18n.tsx";
import { createActionCtxVoid } from "@/business/action-performer";
import {
  ArchiveRegular,
  CodeBlockRegular,
  DatabaseLinkRegular,
} from "@fluentui/react-icons";
import { sortBy } from "lodash-es";
import { CardGrid } from "@/components/layout/CardGrid.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { useDatabaseDrivers } from "@/components/business/db";
import { MessageBox } from "@/components/core/MessageBox.tsx";

export function AdminDbDriverListPage() {
  const { data: driversRaw } = useDatabaseDrivers();
  const { t } = useAppI18n();

  const data = sortBy(driversRaw ?? [], (it) => it.name);
  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: undefined,
    eyebrow: undefined,
    title: t("adminDbDriverListPage_title"),
    titleIcon: <DatabaseLinkRegular />,
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      verticalSpacing={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <MessageBox intent={"info"}>
        {t("adminDbDriverListPage_descriptionLine1")}
        <br />
        {t("adminDbDriverListPage_descriptionLine2")}
      </MessageBox>

      <CardGrid
        data={data}
        renderName={(item) => <Text weight="semibold">{item.name}</Text>}
        renderDescription={(item) => item.id}
        renderBody={(item) => (
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "min-content 1fr",
              alignItems: "center",
              columnGap: tokens.spacingHorizontalS,
            }}
          >
            <Text>
              <ArchiveRegular />
            </Text>{" "}
            <Caption2>{item.location}</Caption2>
            <Text>
              <CodeBlockRegular />
            </Text>{" "}
            <Caption2>{item.className}</Caption2>
          </div>
        )}
        renderEmpty={() => t("adminDbDriverListPage_empty")}
      />
    </ViewLayoutContained>
  );
}
