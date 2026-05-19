import { useNavigate } from "@tanstack/react-router";
import { ActorDetails, AuthRole } from "@medatarun/ui/business/actor";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Table,
  TableBody,
  TableCell,
  TableRow,
  tokens,
} from "@fluentui/react-components";
import { SectionTable } from "@medatarun/ui/components/layout/SecionTable.tsx";
import { SectionTitle } from "@medatarun/ui/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { formatLocalDateTime, toProblem } from "@seij/common-types";
import { MissingInformation } from "@medatarun/ui/components/core/MissingInformation.tsx";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import {
  PropertiesForm,
  PropertyLabel,
  PropertyValue,
} from "@medatarun/ui/components/layout/PropertiesForm.tsx";
import { Key } from "@medatarun/ui/components/core/Key.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader.tsx";
import { PersonKeyRegular } from "@fluentui/react-icons";
import { ActionMenuButton } from "@medatarun/ui/components/business/actions/ActionMenuButton.tsx";
import { type ActionCtx } from "@medatarun/ui/business/action-performer";
import {
  createActionCtxActor,
  createActionCtxActorRole,
  createDisplayedSubjectActor,
} from "@medatarun/ui/business/auth_actor/actor.actioncontexts.ts";
import { useActionRegistry } from "@medatarun/ui/components/business/actions";
import {
  useActor,
  useRoleRegistry,
} from "@medatarun/ui/components/business/auth-actor";

export function AdminActorEditPage({ actorId }: { actorId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const actorResult = useActor(actorId);

  if (actorResult.isPending) return null;
  if (actorResult.error)
    return <ErrorBox error={toProblem(actorResult.error)} />;

  const actor = actorResult.data;
  const actorActions = actionRegistry.findActionDescriptors([
    "auth/actor_enable",
    "auth/actor_disable",
  ]);

  const displayedSubject = createDisplayedSubjectActor(actor.id);
  const actionCtxPage = createActionCtxActor(actor, displayedSubject);
  const actionCtxRole = (role: AuthRole) =>
    createActionCtxActorRole(actor, role, displayedSubject);

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton onClick={() => navigate({ to: "/admin/actors" })}>
          {t("adminActorPage_breadcrumb")}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton current={true}>
          {t("adminActorPage_eyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadcrumb,
    title: actor.fullname,
    titleIcon: <PersonKeyRegular />,
    actions: {
      label: t("adminActorPage_actions"),
      itemActions: actorActions,
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
      <PropertiesForm>
        <PropertyLabel>{t("adminActorPage_fullname")}</PropertyLabel>
        <PropertyValue>{actor.fullname}</PropertyValue>

        <PropertyLabel>{t("adminActorPage_issuer")}</PropertyLabel>
        <PropertyValue>{actor.issuer}</PropertyValue>

        <PropertyLabel>{t("adminActorPage_subject")}</PropertyLabel>
        <PropertyValue>{actor.subject}</PropertyValue>

        <PropertyLabel>{t("adminActorPage_email")}</PropertyLabel>
        <PropertyValue>
          {actor.email ? (
            actor.email
          ) : (
            <MissingInformation>{t("adminActorPage_empty")}</MissingInformation>
          )}
        </PropertyValue>

        <PropertyLabel>{t("adminActorPage_status")}</PropertyLabel>
        <PropertyValue>
          {actor.disabledAt
            ? `${t("adminActorPage_statusDisabledAt")} ${formatLocalDateTime(actor.disabledAt)}`
            : t("adminActorPage_statusActive")}
        </PropertyValue>

        <PropertyLabel>{t("adminActorPage_createdAt")}</PropertyLabel>
        <PropertyValue>{formatLocalDateTime(actor.createdAt)}</PropertyValue>

        <PropertyLabel>{t("adminActorPage_lastSeenAt")}</PropertyLabel>
        <PropertyValue>{formatLocalDateTime(actor.lastSeenAt)}</PropertyValue>
      </PropertiesForm>

      <SectionTitle
        icon={undefined}
        actions={["auth/actor_add_role"]}
        actionCtx={actionCtxPage}
      >
        {t("adminActorPage_rolesTitle")}
      </SectionTitle>
      <SectionTable>
        <ActorRolesTable actor={actor} actionCtxRole={actionCtxRole} />
      </SectionTable>
    </ViewLayoutContained>
  );
}

function ActorRolesTable({
  actor,
  actionCtxRole,
}: {
  actor: ActorDetails;
  actionCtxRole: (role: AuthRole) => ActionCtx;
}) {
  // Hooks
  const { t } = useAppI18n();
  const roleRegistry = useRoleRegistry();
  const actionRegistry = useActionRegistry();

  // Derived
  const roleActions = actionRegistry.findActionDescriptors([
    "auth/actor_delete_role",
  ]);
  const rolesItems = roleRegistry.searchRolesByIdsSorted(actor.roles);

  if (rolesItems.length === 0) {
    return (
      <p>
        <MissingInformation>
          {t("adminActorPage_rolesEmpty")}
        </MissingInformation>
      </p>
    );
  }

  return (
    <Table>
      <TableBody>
        {rolesItems.map((role) => {
          return (
            <TableRow
              key={role.id}
              style={{ border: "1px solid " + tokens.colorNeutralStroke2 }}
            >
              <TableCell>
                <div>{role.label}</div>
                <div>
                  <Key value={role.key} />
                </div>
              </TableCell>
              <TableCell style={{ width: "3em", textAlign: "right" }}>
                <ActionMenuButton
                  itemActions={roleActions}
                  actionCtx={actionCtxRole(role)}
                />
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}
