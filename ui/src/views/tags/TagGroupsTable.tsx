import {
  makeStyles,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
  tokens,
} from "@fluentui/react-components";
import { useAppI18n } from "@/services/appI18n.tsx";
import type { TagGroup } from "@/business/tag";
import { type ActionCtx } from "@/business/action-performer";
import { Key } from "@/components/core/Key.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { useActionRegistry } from "@/components/business/actions";

const useStyles = makeStyles({
  titleCell: {},
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right",
  },
});

export function TagGroupsTable({
  tagGroups,
  onClick,
  actionCtxTagGroup,
}: {
  tagGroups: TagGroup[];
  onClick: (tagGroupId: string) => void;
  actionCtxTagGroup: (tagGroup: TagGroup) => ActionCtx;
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActionDescriptors([
    "tag_group_update_key",
    "tag_group_delete",
  ]);
  const styles = useStyles();
  const detailLevelContext = useDetailLevelContext();

  return (
    <div>
      {tagGroups.length === 0 ? (
        <p style={{ paddingTop: tokens.spacingVerticalM }}>
          <Text italic>{t("tagGroupEdit_emptyTagGroups")}</Text>
        </p>
      ) : null}
      <Table>
        <TableBody>
          {tagGroups.map((tagGroup) => {
            return (
              <TableRow key={tagGroup.id}>
                <TableCell
                  className={styles.titleCell}
                  onClick={() => onClick(tagGroup.id)}
                >
                  <div>{tagGroup.name ?? <Key value={tagGroup.key} />}</div>
                  <div>
                    {tagGroup.name && detailLevelContext.isDetailLevelTech ? (
                      <Key value={tagGroup.key} />
                    ) : null}
                  </div>
                </TableCell>
                <TableCell className={styles.actionCell}>
                  <ActionMenuButton
                    itemActions={itemActions}
                    actionCtx={actionCtxTagGroup(tagGroup)}
                  />
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
}
