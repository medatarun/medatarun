import {Table, TableBody, TableCell, TableRow, Text} from "@fluentui/react-components";
import {RelationshipDescription} from "./RelationshipDescription.tsx";
import {type RelationshipDefSummaryDto, useActionRegistry} from "../../business";
import {useModelContext} from "./ModelContext.tsx";
import {ActionsBar} from "./ActionsBar.tsx";
import {ActionMenuButton} from "./TypesTable.tsx";

export function RelationshipsTable({relationships}:{relationships:RelationshipDefSummaryDto[]}) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions("relationship")
  return <div>
    <div>
      <ActionsBar location="model.relationships" params={{modelKey: model.id}}/>
    </div>
    <div>
      {relationships.length == 0 ? <Text italic>No relationships involved</Text> : null}
    </div>
    <div>
      <Table size="small">
        <TableBody>{relationships
          .map(r => <TableRow key={r.id}>
            <TableCell style={{width: "20em", wordBreak:"break-all"}}>{r.name ?? r.id}</TableCell>
            <TableCell style={{width: "auto"}}><div><RelationshipDescription rel={r}/></div><div><code>{r.id}</code></div></TableCell>
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