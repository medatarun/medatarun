import {
  makeStyles,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
  tokens,
} from "@fluentui/react-components";
import { useNavigate } from "@tanstack/react-router";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { useActionRegistry } from "@/business/action_registry";
import { type TagScopeRef, useTags } from "@/business/tag";
import {
  createActionTemplateTag,
  detailActionLocation,
} from "./tag.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import { type ActionDisplayedSubject } from "@/components/business/actions/ActionPerformer.tsx";
import { Key } from "@/components/core/Key.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

const useStyles = makeStyles({
  titleCell: {},
  descriptionCell: {},
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right",
  },
});

export function TagsTable({
  scope,
  tagGroupId,
  displayedSubject,
}: {
  scope: TagScopeRef;
  tagGroupId?: string;
  displayedSubject: ActionDisplayedSubject;
}) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const tagsResult = useTags(scope);
  const styles = useStyles();
  const detailLevelContext = useDetailLevelContext();

  if (tagsResult.isPending) return null;
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)} />;

  const items = tagsResult.tags.findTagsByScope(scope, tagGroupId);

  const handleClickTag = (tagId: string) => {
    navigate({
      to: "/tags/$tagId",
      params: { tagId: tagId },
    });
  };

  return (
    <div>
      {items.length === 0 ? (
        <p style={{ paddingTop: tokens.spacingVerticalM }}>
          <Text italic>
            {tagGroupId == null
              ? t("tagsTable_emptyScope")
              : t("tagsTable_emptyGroup")}
          </Text>
        </p>
      ) : null}
      <Table>
        <TableBody>
          {items.map((tag) => (
            <TableRow key={tag.id}>
              <TableCell
                className={styles.titleCell}
                onClick={() => handleClickTag(tag.id)}
              >
                {tag.name ?? (
                  <div>
                    <Key value={tag.key} />
                  </div>
                )}
                {tag.name && detailLevelContext.isDetailLevelTech ? (
                  <div>
                    <Key value={tag.key} />
                  </div>
                ) : null}
              </TableCell>
              <TableCell className={styles.actionCell}>
                <ActionMenuButton
                  itemActions={actionRegistry.findActions(
                    detailActionLocation(tag),
                  )}
                  actionParams={createActionTemplateTag(tag.id)}
                  displayedSubject={displayedSubject}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
