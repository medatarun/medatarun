import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { AuthRole, useRoleList } from "@/business/actor";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
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
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  createActionTemplateRole,
  createActionTemplateRoleList,
  createDisplayedSubjectRole,
} from "@/components/business/actor/actor.actions.ts";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";

export function AuthRolesPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const rolesResult = useRoleList();
  const actions = actionRegistry.findActions(ActionUILocations.auth_roles);

  if (rolesResult.isPending) return null;
  if (rolesResult.error) return <ErrorBox error={toProblem(rolesResult.error)} />;

  const roleItems = rolesResult.data.items.map((dto) => new AuthRole(dto));
  const handleClickRole = (roleId: string) => {
    navigate({ to: "/admin/roles/$roleId", params: { roleId } });
  };

  return (
    <ViewLayoutContained
      title={
        <ViewTitle eyebrow={t("authRolesPage_eyebrow")}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              paddingRight: tokens.spacingHorizontalL,
            }}
          >
            <div>{t("authRolesPage_title")}</div>
            <div>
              <ActionMenuButton
                label={t("authRolesPage_actions")}
                itemActions={actions}
                actionParams={createActionTemplateRoleList()}
                displayedSubject={displaySubjectNone}
              />
            </div>
          </div>
        </ViewTitle>
      }
    >
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <ContainedHumanReadable>
            <SectionPaper>{t("authRolesPage_description")}</SectionPaper>
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

  return (
    <Table>
      <TableBody>
        {roles.map((role) => (
          <TableRow key={role.id}>
            <TableCell onClick={() => onClickRole(role.id)}>{role.label}</TableCell>
            <TableCell onClick={() => onClickRole(role.id)}>
              <code>{role.key}</code>
            </TableCell>
            <TableCell onClick={() => onClickRole(role.id)}>
              {role.description ?? ""}
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
