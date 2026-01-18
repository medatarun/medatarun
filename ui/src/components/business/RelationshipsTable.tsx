import {makeStyles, Table, TableBody, TableCell, TableRow, Text, tokens} from "@fluentui/react-components";
import {RelationshipDescription} from "./RelationshipDescription.tsx";
import {type RelationshipDto, useActionRegistry} from "../../business";
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
export function RelationshipsTable({relationships, onClick}:{relationships:RelationshipDto[], onClick:(relationshipId:string)=>void}) {
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
            <TableCell className={styles.titleCell} onClick={()=>onClick(r.id)}>{r.name ?? r.id}</TableCell>
            <TableCell className={styles.flags} onClick={()=>onClick(r.id)}>{" "}</TableCell>
            <TableCell className={styles.descriptionCell} onClick={()=>onClick(r.id)}><div><RelationshipDescription rel={r}/></div>{ isDetailLevelTech && <div><code>{r.id}</code></div> }</TableCell>
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