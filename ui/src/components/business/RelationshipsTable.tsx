import {Table, TableBody, TableCell, TableRow, Text} from "@fluentui/react-components";
import {RelationshipDescription} from "./RelationshipDescription.tsx";
import type {RelationshipDefSummaryDto} from "../../business";

export function RelationshipsTable({relationships}:{relationships:RelationshipDefSummaryDto[]}) {
  return <div>
    <div>
      {relationships.length == 0 ? <Text italic>No relationships involved</Text> : null}
    </div>
    <div>
      <Table size="small">
        <TableBody>{relationships
          .map(r => <TableRow key={r.id}>
            <TableCell style={{width: "10em", wordBreak:"break-all"}}>{r.name ?? r.id}</TableCell>
            <TableCell style={{width: "auto"}}><RelationshipDescription rel={r}/></TableCell>
          </TableRow>)}</TableBody>
      </Table>
    </div>
  </div>
}