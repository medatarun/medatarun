import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  ActorDetails,
  AuthRole,
  useActor,
  useRoleRegistry,
} from "@/business/actor";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Table,
  TableBody,
  TableCell,
  TableRow,
} from "@fluentui/react-components";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox } from "@seij/common-ui";
import { formatLocalDateTime, toProblem } from "@seij/common-types";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import { Key } from "@/components/core/Key.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { PersonKeyRegular } from "@fluentui/react-icons";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { type ActionCtx } from "@/components/business/actions";
import {
  createActionCtxActor,
  createActionCtxActorRole,
  createDisplayedSubjectActor,
} from "@/business/auth_actor/actor.actioncontexts.ts";

export function AdminActorEditPage({ actorId }: { actorId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const actorResult = useActor(actorId);

  if (actorResult.isPending) return null;
  if (actorResult.error)
    return <ErrorBox error={toProblem(actorResult.error)} />;

  const actor = actorResult.data;
  const actorActions = actionRegistry.findActions(ActionUILocations.auth_actor);

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
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <SectionPaper>
        <PropertiesForm>
          <div>{t("adminActorPage_fullname")}</div>
          <div>{actor.fullname}</div>

          <div>{t("adminActorPage_issuer")}</div>
          <div>{actor.issuer}</div>

          <div>{t("adminActorPage_subject")}</div>
          <div>{actor.subject}</div>

          <div>{t("adminActorPage_email")}</div>
          <div>
            {actor.email ? (
              actor.email
            ) : (
              <MissingInformation>
                {t("adminActorPage_empty")}
              </MissingInformation>
            )}
          </div>

          <div>{t("adminActorPage_status")}</div>
          <div>
            {actor.disabledAt
              ? `${t("adminActorPage_statusDisabledAt")} ${formatLocalDateTime(actor.disabledAt)}`
              : t("adminActorPage_statusActive")}
          </div>

          <div>{t("adminActorPage_createdAt")}</div>
          <div>{formatLocalDateTime(actor.createdAt)}</div>

          <div>{t("adminActorPage_lastSeenAt")}</div>
          <div>{formatLocalDateTime(actor.lastSeenAt)}</div>
        </PropertiesForm>
      </SectionPaper>
      <SectionTitle
        icon={undefined}
        location={ActionUILocations.auth_actor_roles}
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
  const roleActions = actionRegistry.findActions(
    ActionUILocations.auth_actor_role,
  );
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
            <TableRow key={role.id}>
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
