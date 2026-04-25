import { useNavigate } from "@tanstack/react-router";
import type { ActionDescriptor } from "@/business/action_registry";
import { useActorList } from "@/components/business/auth-actor";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  Caption1,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
  tokens,
} from "@fluentui/react-components";
import { ErrorBox } from "@seij/common-ui";
import { formatLocalDateTime, toProblem } from "@seij/common-types";
import {
  type ActionCtx,
  displaySubjectNone,
} from "@/business/action-performer";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import type { ActorInfoDto } from "@/business/actor/actor.dto.ts";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { PersonKeyRegular } from "@fluentui/react-icons";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { createActionCtxActor } from "@/business/auth_actor/actor.actioncontexts.ts";
import { useActionRegistry } from "@/components/business/actions";
import { MessageBox } from "@/components/core/MessageBox.tsx";

export function AdminActorListPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const actorListResult = useActorList();
  const itemActions = actionRegistry.findActionDescriptors([
    "actor_enable",
    "actor_disable",
  ]);

  if (actorListResult.isPending) return null;
  if (actorListResult.error)
    return <ErrorBox error={toProblem(actorListResult.error)} />;

  const actorItems = sortBy(actorListResult.data, (it) =>
    it.fullname.toLowerCase(),
  );
  const handleClickActor = (actorId: string) => {
    navigate({ to: "/admin/actors/$actorId", params: { actorId } });
  };

  const actionCtxActor = (actor: ActorInfoDto): ActionCtx =>
    createActionCtxActor(actor, displaySubjectNone);

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("adminActorsPage_eyebrow"),
    title: t("adminActorsPage_title"),
    titleIcon: <PersonKeyRegular />,
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      verticalSpacing={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <MessageBox intent="info">{t("adminActorsPage_description")}</MessageBox>
      <SectionTable>
        <AdminActorsTable
          actors={actorItems}
          itemActions={itemActions}
          onClickActor={handleClickActor}
          actionCtxActor={actionCtxActor}
        />
      </SectionTable>
    </ViewLayoutContained>
  );
}

function AdminActorsTable({
  actors,
  itemActions,
  onClickActor,
  actionCtxActor,
}: {
  actors: ActorInfoDto[];
  itemActions: ActionDescriptor[];
  onClickActor: (actorId: string) => void;
  actionCtxActor: (actor: ActorInfoDto) => ActionCtx;
}) {
  const { t } = useAppI18n();
  if (actors.length === 0) {
    return (
      <p style={{ paddingTop: tokens.spacingVerticalM }}>
        <Text italic>{t("adminActorsPage_empty")}</Text>
      </p>
    );
  }

  return (
    <Table>
      <TableBody>
        {actors.map((actor) => {
          return (
            <TableRow
              key={actor.id}
              style={{ border: "1px solid " + tokens.colorNeutralStroke2 }}
            >
              <TableCell onClick={() => onClickActor(actor.id)}>
                <div>
                  {actor.fullname}
                  {actor.disabledAt ? (
                    <Caption1 style={{ display: "inline" }}>
                      {" "}
                      - {t("adminActorsPage_disabled")}
                    </Caption1>
                  ) : null}
                </div>
                {actor.email ? <Caption1>{actor.email}</Caption1> : null}
              </TableCell>
              <TableCell
                style={{ width: "9em", textAlign: "right" }}
                onClick={() => onClickActor(actor.id)}
              >
                {formatLocalDateTime(actor.lastSeenAt)}
              </TableCell>
              <TableCell style={{ width: "3em", textAlign: "right" }}>
                <ActionMenuButton
                  itemActions={itemActions}
                  actionCtx={actionCtxActor(actor)}
                />
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}
