import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  AuthRoleDetails,
  useRole,
  useRoleUpdateDescription,
  useRoleUpdateKey,
  useRoleUpdateName,
} from "@/business/actor";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
  tokens,
} from "@fluentui/react-components";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  createActionTemplateRole,
  createDisplayedSubjectRole,
} from "@/components/business/actor/actor.actions.ts";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";

export function AuthRolePage({ roleId }: { roleId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const roleResult = useRole("id:" + roleId);
  const roleUpdateName = useRoleUpdateName();
  const roleUpdateKey = useRoleUpdateKey();
  const roleUpdateDescription = useRoleUpdateDescription();

  if (roleResult.isPending) return null;
  if (roleResult.error) return <ErrorBox error={toProblem(roleResult.error)} />;

  const details = new AuthRoleDetails(roleResult.data);
  const role = details.role;
  const actions = actionRegistry.findActions(ActionUILocations.auth_role);

  const handleChangeName = (value: string) => {
    return roleUpdateName.mutateAsync({ roleRef: "id:" + role.id, value });
  };
  const handleChangeKey = (value: string) => {
    return roleUpdateKey.mutateAsync({ roleRef: "id:" + role.id, value });
  };
  const handleChangeDescription = (value: string) => {
    return roleUpdateDescription.mutateAsync({
      roleRef: "id:" + role.id,
      value,
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
                  itemActions={actions}
                  actionParams={createActionTemplateRole(role.id)}
                  displayedSubject={createDisplayedSubjectRole(role.id)}
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
            <SectionPaper>
              <PropertiesForm>
                <div>{t("authRolePage_keyLabel")}</div>
                <div>
                  <InlineEditSingleLine
                    value={role.key}
                    onChange={handleChangeKey}
                  >
                    <Text>
                      <code>{role.key}</code>
                    </Text>
                  </InlineEditSingleLine>
                </div>
                <div>{t("authRolePage_identifierLabel")}</div>
                <div>
                  <Text>
                    <code>{role.id}</code>
                  </Text>
                </div>
                <div>{t("authRolePage_permissionsLabel")}</div>
                <div>{details.permissions.join(", ")}</div>
              </PropertiesForm>
            </SectionPaper>
            <SectionPaper topspacing="XXXL" nopadding>
              <InlineEditDescription
                value={role.description}
                placeholder={t("authRolePage_descriptionPlaceholder")}
                onChange={handleChangeDescription}
              />
            </SectionPaper>
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}
