import {Table, TableBody, TableCell, TableRow, Text, tokens} from "@fluentui/react-components";
import {RelationshipDescription} from "./RelationshipDescription.tsx";
import {type RelationshipDefSummaryDto, useActionRegistry} from "../../business";
import {useModelContext} from "./ModelContext.tsx";
import {ActionMenuButton} from "./TypesTable.tsx";
import {useDetailLevelContext} from "./DetailLevelContext.tsx";

export function RelationshipsTable({relationships}:{relationships:RelationshipDefSummaryDto[]}) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions("relationship")
  const {isDetailLevelTech} = useDetailLevelContext()
  return <div>
    {relationships.length == 0 ? <div style={{paddingTop: tokens.spacingVerticalM}}>
      <Text italic>No relationships in this model.</Text>
    </div> : null}
    <div>
      <Table size="medium">
        <TableBody>{relationships
          .map(r => <TableRow key={r.id}>
            <TableCell style={{width: "20em", wordBreak:"break-all"}}>{r.name ?? r.id}</TableCell>
            <TableCell style={{width: "auto"}}><div><RelationshipDescription rel={r}/></div>{ isDetailLevelTech && <div><code>{r.id}</code></div> }</TableCell>
            <TableCell style={{width: "2em"}}>
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