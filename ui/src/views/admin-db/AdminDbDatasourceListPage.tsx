import { useDatabaseDatasources, useDatabaseDrivers } from "@/business/db";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  Caption2,
  MessageBar,
  MessageBarBody,
  Text,
  tokens,
} from "@fluentui/react-components";
import {
  DatabaseLinkRegular,
  DatabaseRegular,
  LinkRegular,
} from "@fluentui/react-icons";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { ContainedHumanReadable } from "@/components/layout/Contained.tsx";
import { CardGrid } from "@/components/layout/CardGrid.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";

export function AdminDbDatasourceListPage() {
  const { data: dsRaw } = useDatabaseDatasources();
  const { data: driversRaw } = useDatabaseDrivers();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(
    ActionUILocations.admin_database_drivers,
  );
  const { t } = useAppI18n();

  const datasources = sortBy(dsRaw ?? [], (it) => it.id);
  const drivers = sortBy(driversRaw ?? [], (it) => it.id);

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: undefined,
    eyebrow: undefined,
    title: "Databases & datasources",
    titleIcon: <DatabaseRegular />,
    actions: {
      label: "Actions",
      itemActions: actions,
      actionParams: createActionTemplateGeneral(),
      displayedSubject: displaySubjectNone,
    },
  };
  return (
    <ViewLayoutContained title={<ViewLayoutHeader {...headerProps} />}>
      <ContainedHumanReadable>
        <div
          style={{
            paddingLeft: tokens.spacingHorizontalM,
            paddingRight: tokens.spacingHorizontalM,
            paddingTop: tokens.spacingVerticalL,
          }}
        >
          <MessageBar intent={"info"} layout="multiline">
            <MessageBarBody>
              <div>
                Datasources are named connections to your running database (JDBC
                URL and driver).
              </div>
              <div>
                When importing or syncing models, you will select one of these
                datasource.
              </div>
            </MessageBarBody>
          </MessageBar>
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
                  {drivers.find((d) => d.id === item.driver)?.name ??
                    item.driver}
                </Caption2>
                <Text>
                  <LinkRegular />
                </Text>{" "}
                <Caption2>{item.url}</Caption2>
              </div>
            )}
            renderEmpty={() => "Datasources are yet to be created."}
          />
        </div>
      </ContainedHumanReadable>
    </ViewLayoutContained>
  );
}
