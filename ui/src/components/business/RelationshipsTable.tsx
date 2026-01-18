import {makeStyles, Table, TableBody, TableCell, TableRow, Text, tokens} from "@fluentui/react-components";
import {RelationshipDescription} from "./RelationshipDescription.tsx";
import {type RelationshipDefSummaryDto, useActionRegistry} from "../../business";
import {useModelContext} from "./ModelContext.tsx";
import {ActionMenuButton} from "./TypesTable.tsx";
import {useDetailLevelContext} from "./DetailLevelContext.tsx";

const useStyles = makeStyles({
  titleCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "20rem",
    verticalAlign: "baseline",
    wordBreak: "break-all"
  },
  flags: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "1em", verticalAlign: "baseline"
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    "& p": {
      marginTop: 0
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
export function RelationshipsTable({relationships}:{relationships:RelationshipDefSummaryDto[]}) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions("relationship")
  const {isDetailLevelTech} = useDetailLevelContext()
  const styles = useStyles()
  return <div>
    {relationships.length == 0 ? <div style={{paddingTop: tokens.spacingVerticalM}}>
      <Text italic>No relationships in this model.</Text>
    </div> : null}
    <div>
      <Table>
        <TableBody>{relationships
          .map(r => <TableRow key={r.id}>
            <TableCell className={styles.titleCell}>{r.name ?? r.id}</TableCell>
            <TableCell className={styles.flags}>{" "}</TableCell>
            <TableCell className={styles.descriptionCell}><div><RelationshipDescription rel={r}/></div>{ isDetailLevelTech && <div><code>{r.id}</code></div> }</TableCell>
            <TableCell className={styles.actionCell}>
              <ActionMenuButton
                itemActions={itemActions}
                actionParams={{modelKey: model.id, relationshipKey: r.id}}
              />
            </TableCell>
          </TableRow>)}</TableBody>
      </Table>
    </div>
  </div>
}