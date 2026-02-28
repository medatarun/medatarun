import {makeStyles, Table, TableBody, TableCell, TableRow, Text, tokens} from "@fluentui/react-components";
import {ActionUILocations, type TagSearchItemDto, useActionRegistry} from "../../business";
import {ActionMenuButton} from "./TypesTable.tsx";
import {Markdown} from "../core/Markdown.tsx";
import {createActionTemplateTag} from "./actionTemplates.ts";

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

export function TagsTable({tags, onClick}: {
  tags: TagSearchItemDto[],
  onClick: (tagId: string) => void
}) {
  const actionRegistry = useActionRegistry()
  const itemActions = actionRegistry.findActions(ActionUILocations.tag_managed_detail)
  const styles = useStyles()

  return <div>
    {tags.length === 0 ? <p style={{paddingTop: tokens.spacingVerticalM}}>
      <Text italic>No tags in this group.</Text>
    </p> : null}
    <Table>
      <TableBody>
        {tags.map(tag =>
          <TableRow key={tag.id}>
            <TableCell
              className={styles.titleCell}
              onClick={() => onClick(tag.id)}>
              {tag.name ?? tag.key}
            </TableCell>
            <TableCell
              className={styles.descriptionCell}
              onClick={() => onClick(tag.id)}>
              <div><Markdown value={tag.description}/></div>
              <div><code>{tag.key}</code></div>
            </TableCell>
            <TableCell className={styles.actionCell}>
              <ActionMenuButton
                itemActions={itemActions}
                actionParams={createActionTemplateTag(tag.id)}
              />
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  </div>
}
