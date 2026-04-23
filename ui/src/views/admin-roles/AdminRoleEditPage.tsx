import { useNavigate } from "@tanstack/react-router";
import { useActionRegistry } from "@/business/action_registry";
import {
  AuthRoleDetails,
  useRole,
  useRoleUpdateDescription,
  useRoleUpdateName,
} from "@/business/actor";
import { usePermissionRegistry } from "@/business/config";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Caption1,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
} from "@fluentui/react-components";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox, InfoBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { Key } from "@/components/core/Key.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { LockClosedRegular } from "@fluentui/react-icons";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { type ActionCtx } from "@/business/action-performer";
import {
  createActionCtxRole,
  createActionCtxRolePermission,
  createDisplayedSubjectRole,
} from "@/business/auth_actor/actor.actioncontexts.ts";

export function AdminRoleEditPage({ roleId }: { roleId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const roleResult = useRole(roleId);
  const roleUpdateName = useRoleUpdateName();
  const roleUpdateDescription = useRoleUpdateDescription();

  if (roleResult.isPending) return null;
  if (roleResult.error) return <ErrorBox error={toProblem(roleResult.error)} />;

  const details = new AuthRoleDetails(roleResult.data);
  const role = details.role;

  const handleChangeName = (value: string) => {
    return roleUpdateName.mutateAsync({ roleId: role.id, value: value });
  };
  const handleChangeDescription = (value: string) => {
    return roleUpdateDescription.mutateAsync({
      roleId: role.id,
      value: value,
    });
  };

  const displayedSubject = createDisplayedSubjectRole(role.id);
  const actionCtxPage = createActionCtxRole(role, displayedSubject);
  const actionCtxPermission = (permissionKey: string) =>
    createActionCtxRolePermission(role, permissionKey, displayedSubject);

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton onClick={() => navigate({ to: "/admin/roles" })}>
          {t("authRolePage_breadcrumb")}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton current={true}>
          {t("authRolePage_eyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadcrumb,
    titleIcon: <LockClosedRegular />,
    title: (
      <InlineEditSingleLine value={role.name} onChange={handleChangeName}>
        {role.name ? (
          role.name
        ) : (
          <MissingInformation>{role.key}</MissingInformation>
        )}
      </InlineEditSingleLine>
    ),
    actions: {
      label: t("authRolePage_actions"),
      itemActions: actionRegistry.findActionDescriptors([
        "role_update_key",
        "role_delete",
      ]),
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <SectionPaper topspacing="XXXL" nopadding>
        <InlineEditDescription
          value={role.description}
          placeholder={t("authRolePage_descriptionPlaceholder")}
          onChange={handleChangeDescription}
        />
      </SectionPaper>
      <SectionTitle
        icon={undefined}
        actions={["role_add_permission"]}
        actionCtx={actionCtxPage}
      >
        {t("authRolePage_permissionsLabel")}
      </SectionTitle>
      <SectionTable>
        <PermissionTable
          permissions={details.permissions}
          actionCtxPermission={actionCtxPermission}
        />
      </SectionTable>

      <SectionTitle icon={undefined}>
        {t("authRolePage_permissionsImpliedLabel")}
      </SectionTitle>

      <SectionTable>
        <InfoBox intent={"info"}>
          {t("authRolePage_permissionsImpliedDescription")}
        </InfoBox>
        <PermissionImpliedTable permissions={details.permissions} />
      </SectionTable>

      <ViewLayoutTechnicalInfos
        technicalKey={role.key}
        id={role.id}
        keyLabel={t("authRolePage_keyLabel")}
        idLabel={t("authRolePage_identifierLabel")}
      />
    </ViewLayoutContained>
  );
}

function PermissionTable({
  permissions,
  actionCtxPermission,
}: {
  permissions: string[];
  actionCtxPermission: (permissionKey: string) => ActionCtx;
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const permissionActions = actionRegistry.findActionDescriptors([
    "role_delete_permission",
  ]);
  const { registry: permissionRegistry } = usePermissionRegistry();

  if (permissions.length === 0) {
    return (
      <p>
        <MissingInformation>
          {t("authRolePage_permissionsEmpty")}
        </MissingInformation>
      </p>
    );
  }

  return (
    <Table>
      <TableBody>
        {permissions.map((permissionKey) => {
          const permissionName = permissionRegistry.findName(permissionKey);
          const permissionDescription =
            permissionRegistry.findDescription(permissionKey);
          return (
            <TableRow key={permissionKey}>
              <TableCell>
                <p>
                  {permissionName ? <Text>{permissionName}</Text> : null}
                  {permissionDescription ? (
                    <div>
                      <Caption1>{permissionDescription}</Caption1>
                    </div>
                  ) : null}
                  <div>
                    <Key value={permissionKey} />
                  </div>
                </p>
              </TableCell>
              <TableCell style={{ width: "3em", textAlign: "right" }}>
                <ActionMenuButton
                  itemActions={permissionActions}
                  actionCtx={actionCtxPermission(permissionKey)}
                />
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}

function PermissionImpliedTable({ permissions }: { permissions: string[] }) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const permissionActions = actionRegistry.findActionDescriptors([
    "role_delete_permission",
  ]);
  const { registry: permissionRegistry } = usePermissionRegistry();
  const permissionsImplied =
    permissionRegistry.findImpliedPermissions(permissions);

  if (permissionsImplied.length === 0) {
    return (
      <p>
        <MissingInformation>
          {t("authRolePage_permissionsImpliedEmpty")}
        </MissingInformation>
      </p>
    );
  }

  return (
    <Table>
      <TableBody>
        {permissionsImplied.map((permissionKey) => {
          const permissionName = permissionRegistry.findName(permissionKey);
          const permissionDescription =
            permissionRegistry.findDescription(permissionKey);
          return (
            <TableRow key={permissionKey}>
              <TableCell>
                <p>
                  {permissionName ? <Text>{permissionName}</Text> : null}
                  {permissionDescription ? (
                    <div>
                      <Caption1>{permissionDescription}</Caption1>
                    </div>
                  ) : null}
                  <div>
                    <Key value={permissionKey} />
                  </div>
                </p>
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}
