import { useDatabaseDatasources, useDatabaseDrivers } from "@/business/db";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { Caption2, Text, tokens } from "@fluentui/react-components";
import {
  DatabaseLinkRegular,
  DatabaseRegular,
  LinkRegular,
} from "@fluentui/react-icons";
import { createActionCtxVoid } from "@/components/business/actions";
import { CardGrid } from "@/components/layout/CardGrid.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutPageInfo } from "@/components/layout/ViewLayoutPageInfo.tsx";

export function AdminDbDatasourceListPage() {
  const { data: dsRaw } = useDatabaseDatasources();
  const { data: driversRaw } = useDatabaseDrivers();
  const { t } = useAppI18n();

  const datasources = sortBy(dsRaw ?? [], (it) => it.id);
  const drivers = sortBy(driversRaw ?? [], (it) => it.id);

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: undefined,
    eyebrow: undefined,
    title: t("adminDbDatasourceListPage_title"),
    titleIcon: <DatabaseRegular />,
  };
  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <ViewLayoutPageInfo>
        <div>{t("adminDbDatasourceListPage_descriptionLine1")}</div>
        <div>{t("adminDbDatasourceListPage_descriptionLine2")}</div>
      </ViewLayoutPageInfo>
      <CardGrid
        data={datasources}
        renderName={(item) => <Text weight="semibold">{item.id}</Text>}
        renderDescription={(item) => null}
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
              <DatabaseLinkRegular />
            </Text>{" "}
            <Caption2>
              {drivers.find((d) => d.id === item.driver)?.name ?? item.driver}
            </Caption2>
            <Text>
              <LinkRegular />
            </Text>{" "}
            <Caption2>{item.url}</Caption2>
          </div>
        )}
        renderEmpty={() => t("adminDbDatasourceListPage_empty")}
      />
    </ViewLayoutContained>
  );
}
