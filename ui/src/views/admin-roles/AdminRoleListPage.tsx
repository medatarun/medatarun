import { useNavigate } from "@tanstack/react-router";
import { AuthRole } from "@/business/actor";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
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
} from "@/business/action-performer";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { KeyMultipleRegular, LockClosedRegular } from "@fluentui/react-icons";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { createActionCtxRole } from "@/business/auth_actor/actor.actioncontexts.ts";
import { useActionRegistry } from "@/components/business/actions";
import { useRoleList } from "@/components/business/auth-actor";
import { MessageBox } from "@/components/core/MessageBox.tsx";

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

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("authRolesPage_eyebrow"),
    title: t("authRolesPage_title"),
    titleIcon: <KeyMultipleRegular />,
    actions: {
      label: t("authRolesPage_actions"),
      itemActions: actionRegistry.findActionDescriptors(["role_create"]),
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
        />
      </SectionTable>
    </ViewLayoutContained>
  );
}

function AuthRolesTable({
  roles,
  onClickRole,
  actionCtxRole,
}: {
  roles: AuthRole[];
  onClickRole: (roleId: string) => void;
  actionCtxRole: (role: AuthRole) => ActionCtx;
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const detailActions = actionRegistry.findActionDescriptors([
    "role_update_key",
    "role_update_name",
    "role_update_description",
    "role_delete",
  ]);

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
                {role.label} {role.managedRole && <LockClosedRegular />}
              </p>
            </TableCell>
            <TableCell style={{ width: "3em", textAlign: "right" }}>
              <ActionMenuButton
                itemActions={detailActions}
                actionCtx={actionCtxRole(role)}
              />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
