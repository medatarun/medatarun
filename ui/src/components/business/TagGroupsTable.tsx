import {makeStyles, Table, TableBody, TableCell, TableRow, Text, tokens} from "@fluentui/react-components";
import {ActionUILocations, type TagGroupListItemDto, useActionRegistry} from "../../business";
import {ActionMenuButton} from "./TypesTable.tsx";
import {Markdown} from "../core/Markdown.tsx";
import {createActionTemplateTagGroup} from "./actionTemplates.ts";

const useStyles = makeStyles({
  titleCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "20rem",
    verticalAlign: "baseline",
    wordBreak: "break-all"
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    "& p": {
      marginTop: 0
    },
    "& p:last-child": {
      marginBottom: 0
    }
  },
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right"
  }
})

export function TagGroupsTable({tagGroups, onClick}: {
  tagGroups: TagGroupListItemDto[],
  onClick: (tagGroupId: string) => void
}) {
  const actionRegistry = useActionRegistry()
  const itemActions = actionRegistry.findActions(ActionUILocations.tag_managed_group_detail)
  const styles = useStyles()

  return <div>
    {tagGroups.length === 0 ? <p style={{paddingTop: tokens.spacingVerticalM}}>
      <Text italic>No global tag groups.</Text>
    </p> : null}
    <Table>
      <TableBody>
        {tagGroups.map(tagGroup =>
          <TableRow key={tagGroup.id}>
            <TableCell
              className={styles.titleCell}
              onClick={() => onClick(tagGroup.id)}>
              {tagGroup.name ?? tagGroup.key}
            </TableCell>
            <TableCell
              className={styles.descriptionCell}
              onClick={() => onClick(tagGroup.id)}>
              <div><Markdown value={tagGroup.description}/></div>
              <div><code>{tagGroup.key}</code></div>
            </TableCell>
            <TableCell className={styles.actionCell}>
              <ActionMenuButton
                itemActions={itemActions}
                actionParams={createActionTemplateTagGroup(tagGroup.id)}
              />
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  </div>
}
