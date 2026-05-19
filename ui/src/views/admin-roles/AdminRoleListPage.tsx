import { useNavigate } from "@tanstack/react-router";
import { AuthRole } from "@medatarun/ui/business/actor";
import { SectionTable } from "@medatarun/ui/components/layout/SecionTable.tsx";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";
import {
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
  tokens,
} from "@fluentui/react-components";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  type ActionCtx,
  createActionCtxVoid,
  displaySubjectNone,
} from "@medatarun/ui/business/action-performer";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader.tsx";
import {
  ArrowSyncCircleRegular,
  KeyMultipleRegular,
  LockClosedRegular,
} from "@fluentui/react-icons";
import { ActionMenuButton } from "@medatarun/ui/components/business/actions/ActionMenuButton.tsx";
import { createActionCtxRole } from "@medatarun/ui/business/auth_actor/actor.actioncontexts.ts";
import { useActionRegistry } from "@medatarun/ui/components/business/actions";
import { useRoleList } from "@medatarun/ui/components/business/auth-actor";
import { MessageBox } from "@medatarun/ui/components/core/MessageBox.tsx";
import { ActionDescriptor } from "../../business/action-registry";

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

  const actionCtxPage = createActionCtxVoid();
  const actionCtxRole = (role: AuthRole) =>
    createActionCtxRole(role, displaySubjectNone);
  const actionListRole = (role: AuthRole) =>
    actionRegistry.findActionDescriptors([
      role.managedRole ? undefined : "auth/role_update_name",
      role.managedRole ? undefined : "auth/role_update_description",
      role.managedRole ? undefined : "auth/role_update_key",
      "auth/role_update_autoassign",
      role.managedRole ? undefined : "auth/role_delete",
    ]);

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("authRolesPage_eyebrow"),
    title: t("authRolesPage_title"),
    titleIcon: <KeyMultipleRegular />,
    actions: {
      label: t("authRolesPage_actions"),
      itemActions: actionRegistry.findActionDescriptors(["auth/role_create"]),
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      verticalSpacing={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <MessageBox intent={"info"}>{t("authRolesPage_description")}</MessageBox>
      <SectionTable>
        <AuthRolesTable
          roles={roleItems}
          onClickRole={handleClickRole}
          actionCtxRole={actionCtxRole}
          actionListRole={actionListRole}
        />
      </SectionTable>
    </ViewLayoutContained>
  );
}

function AuthRolesTable({
  roles,
  onClickRole,
  actionCtxRole,
  actionListRole,
}: {
  roles: AuthRole[];
  onClickRole: (roleId: string) => void;
  actionCtxRole: (role: AuthRole) => ActionCtx;
  actionListRole: (role: AuthRole) => ActionDescriptor[];
}) {
  const { t } = useAppI18n();
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
          <TableRow
            key={role.id}
            style={{ border: "1px solid " + tokens.colorNeutralStroke2 }}
          >
            <TableCell
              style={{ width: "10em" }}
              onClick={() => onClickRole(role.id)}
            >
              <p>
                {role.label} {role.managedRole && <LockClosedRegular />}{" "}
                {role.autoAssign && <ArrowSyncCircleRegular />}
              </p>
            </TableCell>
            <TableCell style={{ width: "3em", textAlign: "right" }}>
              <ActionMenuButton
                itemActions={actionListRole(role)}
                actionCtx={actionCtxRole(role)}
              />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
