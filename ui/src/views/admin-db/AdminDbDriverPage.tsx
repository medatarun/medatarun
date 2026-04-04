import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { Caption2, Text, tokens } from "@fluentui/react-components";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { useDatabaseDrivers } from "@/business/db";
import {
  ArchiveRegular,
  CodeBlockRegular,
  DatabaseLinkRegular,
} from "@fluentui/react-icons";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { InfoBox } from "@seij/common-ui";
import { ContainedHumanReadable } from "@/components/layout/Contained.tsx";
import { sortBy } from "lodash-es";
import { CardGrid } from "@/components/layout/CardGrid.tsx";

export function AdminDbDriverPage() {
  const { data: driversRaw } = useDatabaseDrivers();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(
    ActionUILocations.admin_database_drivers,
  );
  const { t } = useAppI18n();

  const data = sortBy(driversRaw ?? [], (it) => it.name);

  return (
    <ViewLayoutContained
      title={
        <div>
          <ViewTitle>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div>
                <span>
                  <DatabaseLinkRegular />
                </span>{" "}
                Database drivers{" "}
              </div>
              <div>
                <ActionMenuButton
                  label={"Actions"}
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
        <div
          style={{
            paddingLeft: tokens.spacingHorizontalM,
            paddingRight: tokens.spacingHorizontalM,
            paddingTop: tokens.spacingVerticalL,
          }}
        >
          <InfoBox intent={"info"}>
            <Text>
              Medatarun uses JDBC drivers to talk to most existing database
              software.
              <br />
              Here is a recap of the installed drivers. To add new ones, check
              the documentation.
            </Text>
          </InfoBox>
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
        </div>
      </ContainedHumanReadable>
    </ViewLayoutContained>
  );
}
