import {makeStyles, Table, TableBody, TableCell, TableRow, Text, tokens} from "@fluentui/react-components";
import {useNavigate} from "@tanstack/react-router";
import {ErrorBox} from "@seij/common-ui";
import {toProblem} from "@seij/common-types";
import {type TagScopeRef, useActionRegistry, useTags} from "../../../business";
import {ActionMenuButton} from "../model/TypesTable.tsx";
import {Markdown} from "../../core/Markdown.tsx";
import {createActionTemplateTag, detailActionLocation} from "./tag.actions.ts";

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

export function TagsTable({scope, tagGroupId}: {
  scope: TagScopeRef,
  tagGroupId?: string
}) {
  const navigate = useNavigate()
  const actionRegistry = useActionRegistry()
  const tagsResult = useTags(scope)
  const styles = useStyles()

  if (tagsResult.isPending) return null
  if (tagsResult.error) return <ErrorBox error={toProblem(tagsResult.error)}/>

  const items = tagsResult.tags.findTagsByScope(scope, tagGroupId)

  const handleClickTag = (tagId: string) => {
    navigate({
      to: "/tags/$tagId",
      params: {tagId: tagId}
    })
  }

  return <div>
    {items.length === 0 ? <p style={{paddingTop: tokens.spacingVerticalM}}>
      <Text italic>{tagGroupId == null ? "No tags in this scope." : "No tags in this group."}</Text>
    </p> : null}
    <Table>
      <TableBody>
        {items.map(tag =>
          <TableRow key={tag.id}>
            <TableCell
              className={styles.titleCell}
              onClick={() => handleClickTag(tag.id)}>
              {tag.name ?? tag.key}
            </TableCell>
            <TableCell
              className={styles.descriptionCell}
              onClick={() => handleClickTag(tag.id)}>
              <div><Markdown value={tag.description}/></div>
              <div><code>{tag.key}</code></div>
            </TableCell>
            <TableCell className={styles.actionCell}>
              <ActionMenuButton
                itemActions={actionRegistry.findActions(detailActionLocation(tag))}
                actionParams={createActionTemplateTag(tag.id)}
              />
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  </div>
}
