import { useNavigate } from "@tanstack/react-router";
import type { ActionDescriptor } from "@/business/action_registry";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { useActorList } from "@/business/actor";
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import {
  ContainedHumanReadable,
  ContainedMixedScrolling,
  ContainedScrollable,
} from "@/components/layout/Contained.tsx";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
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
import { ErrorBox, InfoBox } from "@seij/common-ui";
import { formatLocalDateTime, toProblem } from "@seij/common-types";
import {
  createActionTemplateActor,
  createDisplayedSubjectActor,
} from "@/components/business/actor/actor.actions.ts";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import type { ActorInfoDto } from "@/business/actor/actor.dto.ts";

export function AdminActorsPage() {
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

  return (
    <ViewLayoutContained
      title={
        <ViewTitle eyebrow={t("adminActorsPage_eyebrow")}>
          {t("adminActorsPage_title")}
        </ViewTitle>
      }
    >
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <ContainedHumanReadable>
            <p></p>
            <InfoBox intent={"info"}>
              {t("adminActorsPage_description")}
            </InfoBox>
            <SectionTitle
              icon={undefined}
              location={ActionUILocations.auth_actors}
              actionParams={{}}
              displayedSubject={displaySubjectNone}
            >
              {""}
            </SectionTitle>
            <SectionTable>
              <AdminActorsTable
                actors={actorItems}
                itemActions={itemActions}
                onClickActor={handleClickActor}
              />
            </SectionTable>
          </ContainedHumanReadable>
        </ContainedScrollable>
      </ContainedMixedScrolling>
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
