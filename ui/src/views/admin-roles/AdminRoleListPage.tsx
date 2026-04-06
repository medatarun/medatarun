import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { AuthRole, useRoleList } from "@/business/actor";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
  tokens,
} from "@fluentui/react-components";
import { ErrorBox, InfoBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  createActionTemplateRole,
  createActionTemplateRoleList,
  createDisplayedSubjectRole,
} from "@/components/business/actor/actor.actions.ts";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { LockClosedRegular } from "@fluentui/react-icons";

export function AdminRoleListPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const rolesResult = useRoleList();

  if (rolesResult.isPending) return null;
  if (rolesResult.error)
    return <ErrorBox error={toProblem(rolesResult.error)} />;

  const roleItems = rolesResult.data.items.map((dto) => new AuthRole(dto));
  const handleClickRole = (roleId: string) => {
    navigate({ to: "/admin/roles/$roleId", params: { roleId } });
  };

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("authRolesPage_eyebrow"),
    title: t("authRolesPage_title"),
    titleIcon: <LockClosedRegular />,
    actions: {
      label: t("authRolesPage_actions"),
      itemActions: actionRegistry.findActions(ActionUILocations.auth_roles),
      actionParams: createActionTemplateRoleList(),
      displayedSubject: displaySubjectNone,
    },
  };

  return (
    <ViewLayoutContained title={<ViewLayoutHeader {...headerProps} />}>
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <ContainedHumanReadable>
            <p></p>
            <InfoBox intent={"info"}>{t("authRolesPage_description")}</InfoBox>
            <SectionTitle
              icon={undefined}
              location={ActionUILocations.auth_roles}
              actionParams={createActionTemplateRoleList()}
              displayedSubject={displaySubjectNone}
            >
              {t("authRolesPage_sectionTitle")}
            </SectionTitle>
            <SectionTable>
              <AuthRolesTable roles={roleItems} onClickRole={handleClickRole} />
            </SectionTable>
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}

function AuthRolesTable({
  roles,
  onClickRole,
}: {
  roles: AuthRole[];
  onClickRole: (roleId: string) => void;
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const detailActions = actionRegistry.findActions(ActionUILocations.auth_role);

  if (roles.length === 0) {
    return (
      <p style={{ paddingTop: tokens.spacingVerticalM }}>
        <Text italic>{t("authRolesPage_empty")}</Text>
      </p>
    );
  }

  const rolesSorted = sortBy(roles, (it) => it.name);

  return (
    <Table>
      <TableBody>
        {rolesSorted.map((role) => (
          <TableRow key={role.id}>
            <TableCell
              style={{ width: "10em" }}
              onClick={() => onClickRole(role.id)}
            >
              <p>{role.label}</p>
            </TableCell>
            <TableCell style={{ width: "3em", textAlign: "right" }}>
              <ActionMenuButton
                itemActions={detailActions}
                actionParams={createActionTemplateRole(role.id)}
                displayedSubject={createDisplayedSubjectRole(role.id)}
              />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
