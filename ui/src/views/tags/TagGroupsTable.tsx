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
import { ActionMenuButton } from "@/components/business/model/TypesTable.tsx";
import { Markdown } from "@/components/core/Markdown.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";

const useStyles = makeStyles({
  titleCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "20rem",
    verticalAlign: "baseline",
    wordBreak: "break-all",
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    "& p": {
      marginTop: 0,
    },
    "& p:last-child": {
      marginBottom: 0,
    },
  },
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
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
                {tagGroup.label}
              </TableCell>
              <TableCell
                className={styles.descriptionCell}
                onClick={() => onClick(tagGroup.id)}
              >
                <div>
                  <Markdown value={tagGroup.description} />
                </div>
                <div>
                  <code>{tagGroup.key}</code>
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
