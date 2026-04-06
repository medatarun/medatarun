import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  AuthRoleDetails,
  useRole,
  useRoleUpdateDescription,
  useRoleUpdateName,
} from "@/business/actor";
import { usePermissionRegistry } from "@/business/config";
import { refid } from "@/business/action_runner";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
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
  tokens,
} from "@fluentui/react-components";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  createActionTemplateRole,
  createDisplayedSubjectRole,
  createDisplayedSubjectRolePermission,
} from "@/components/business/actor/actor.actions.ts";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { Key } from "@/components/core/Key.tsx";

export function AuthRolePage({ roleId }: { roleId: string }) {
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

  const displayedSubject = createDisplayedSubjectRole(role.id);

  const handleChangeName = (value: string) => {
    return roleUpdateName.mutateAsync({ roleId: role.id, value: value });
  };
  const handleChangeDescription = (value: string) => {
    return roleUpdateDescription.mutateAsync({
      roleId: role.id,
      value: value,
    });
  };

  return (
    <ViewLayoutContained
      title={
        <div>
          <div style={{ marginLeft: "-22px" }}>
            <Breadcrumb size="small">
              <BreadcrumbItem>
                <BreadcrumbButton
                  onClick={() => navigate({ to: "/admin/roles" })}
                >
                  {t("authRolePage_breadcrumb")}
                </BreadcrumbButton>
              </BreadcrumbItem>
              <BreadcrumbDivider />
            </Breadcrumb>
          </div>
          <ViewTitle eyebrow={t("authRolePage_eyebrow")}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                paddingRight: tokens.spacingHorizontalL,
              }}
            >
              <div style={{ width: "100%" }}>
                <InlineEditSingleLine
                  value={role.name}
                  onChange={handleChangeName}
                >
                  {role.name ? (
                    role.name
                  ) : (
                    <MissingInformation>{role.key}</MissingInformation>
                  )}
                </InlineEditSingleLine>
              </div>
              <div>
                <ActionMenuButton
                  label={t("authRolePage_actions")}
                  itemActions={actionRegistry.findActions(
                    ActionUILocations.auth_role,
                  )}
                  actionParams={createActionTemplateRole(role.id)}
                  displayedSubject={displayedSubject}
                />
              </div>
            </div>
          </ViewTitle>
        </div>
      }
    >
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <ContainedHumanReadable>
            <SectionPaper topspacing="XXXL" nopadding>
              <InlineEditDescription
                value={role.description}
                placeholder={t("authRolePage_descriptionPlaceholder")}
                onChange={handleChangeDescription}
              />
            </SectionPaper>
            <SectionTitle
              icon={undefined}
              location={ActionUILocations.auth_role_permissions}
              actionParams={createActionTemplateRole(role.id)}
              displayedSubject={displayedSubject}
            >
              {t("authRolePage_permissionsLabel")}
            </SectionTitle>
            <SectionTable>
              <PermissionTable
                roleId={role.id}
                permissions={details.permissions}
              />
            </SectionTable>

            <p
              style={{
                marginTop: "12em",
                borderTop: "1px solid #CCC",
                textAlign: "right",
              }}
            >
              <Caption1 style={{ color: tokens.colorNeutralForeground5 }}>
                {t("authRolePage_keyLabel")}: <Key value={role.key} /> -{" "}
                {t("authRolePage_identifierLabel")}: <code>[{role.id}]</code>
              </Caption1>
            </p>
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}

function createActionTemplateRolePermission(
  roleId: string,
  permissionKey: string,
) {
  return {
    roleRef: refid(roleId),
    permissionKey: { value: permissionKey, readonly: true },
  };
}

function PermissionTable({
  roleId,
  permissions,
}: {
  roleId: string;
  permissions: string[];
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const permissionActions = actionRegistry.findActions(
    ActionUILocations.auth_role_permission,
  );
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
                  actionParams={createActionTemplateRolePermission(
                    roleId,
                    permissionKey,
                  )}
                  displayedSubject={createDisplayedSubjectRolePermission(
                    roleId,
                    permissionKey,
                  )}
                />
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}
