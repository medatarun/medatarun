import { useNavigate } from "@tanstack/react-router";
import type { ActionDescriptor } from "@/business/action_registry";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { useActorList } from "@/business/actor";
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
  createActionTemplateActor,
  createDisplayedSubjectActor,
} from "@/components/business/actor/actor.actions.ts";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import type { ActorInfoDto } from "@/business/actor/actor.dto.ts";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { PersonKeyRegular } from "@fluentui/react-icons";
import { ViewLayoutPageInfo } from "@/components/layout/ViewLayoutPageInfo.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

export function AdminActorListPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const actorListResult = useActorList();
  const itemActions = actionRegistry.findActions(ActionUILocations.auth_actor);

  if (actorListResult.isPending) return null;
  if (actorListResult.error)
    return <ErrorBox error={toProblem(actorListResult.error)} />;

  const actorItems = sortBy(actorListResult.data, (it) =>
    it.fullname.toLowerCase(),
  );
  const handleClickActor = (actorId: string) => {
    navigate({ to: "/admin/actors/$actorId", params: { actorId } });
  };

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("adminActorsPage_eyebrow"),
    title: t("adminActorsPage_title"),
    titleIcon: <PersonKeyRegular />,
    actions: {
      label: t("adminActorsPage_actions"),
      itemActions: actionRegistry.findActions(ActionUILocations.auth_actors),
      actionParams: {},
      displayedSubject: displaySubjectNone,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <ViewLayoutPageInfo>
        {t("adminActorsPage_description")}
      </ViewLayoutPageInfo>
      <SectionTable>
        <AdminActorsTable
          actors={actorItems}
          itemActions={itemActions}
          onClickActor={handleClickActor}
        />
      </SectionTable>
    </ViewLayoutContained>
  );
}

function AdminActorsTable({
  actors,
  itemActions,
  onClickActor,
}: {
  actors: ActorInfoDto[];
  itemActions: ActionDescriptor[];
  onClickActor: (actorId: string) => void;
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
        {actors.map((actor) => (
          <TableRow key={actor.id}>
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
                actionParams={createActionTemplateActor(actor.id)}
                displayedSubject={createDisplayedSubjectActor(actor.id)}
              />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
