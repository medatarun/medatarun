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
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import type { TagGroup } from "@/business/tag";
import { createActionTemplateTagGroup } from "@/components/business/tag/tag.actions.ts";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { Key } from "@/components/core/Key.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

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
}: {
  tagGroups: TagGroup[];
  onClick: (tagGroupId: string) => void;
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions(
    ActionUILocations.tag_group_detail,
  );
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
          {tagGroups.map((tagGroup) => (
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
                  actionParams={createActionTemplateTagGroup(tagGroup.id)}
                  displayedSubject={displaySubjectNone}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
