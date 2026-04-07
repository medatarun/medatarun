import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  Caption2,
  MessageBar,
  MessageBarBody,
  Text,
  tokens,
} from "@fluentui/react-components";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { useDatabaseDrivers } from "@/business/db";
import {
  ArchiveRegular,
  CodeBlockRegular,
  DatabaseLinkRegular,
} from "@fluentui/react-icons";
import { ContainedHumanReadable } from "@/components/layout/Contained.tsx";
import { sortBy } from "lodash-es";
import { CardGrid } from "@/components/layout/CardGrid.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { ViewLayoutPageInfo } from "@/components/layout/ViewLayoutPageInfo.tsx";

export function AdminDbDriverListPage() {
  const { data: driversRaw } = useDatabaseDrivers();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(
    ActionUILocations.admin_database_drivers,
  );
  const { t } = useAppI18n();

  const data = sortBy(driversRaw ?? [], (it) => it.name);

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: undefined,
    eyebrow: undefined,
    title: "Databases drivers",
    titleIcon: <DatabaseLinkRegular />,
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
          <ViewLayoutPageInfo>
              <Text>
                Medatarun uses JDBC drivers to talk to most existing database
                software.
                <br />
                Here is a recap of the installed drivers. To add new ones, check
                the documentation.
              </Text>
          </ViewLayoutPageInfo>

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
            renderEmpty={() => "Drivers are yet to be installed."}
          />

      </ContainedHumanReadable>
    </ViewLayoutContained>
  );
}
