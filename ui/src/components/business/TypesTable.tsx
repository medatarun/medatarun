import type {TypeDto} from "../../business/model.tsx";
import {Table, TableBody, TableCell, TableRow, Text} from "@fluentui/react-components";

export function TypesTable({types}: { types: TypeDto[] }) {
  return <div>
    <div>
      {types.length == 0 ? <Text italic>No types found</Text> : null}
    </div>
    <div><Table size="small" style={{marginBottom: "1em"}}>
      <TableBody>
        {
          types.map(t => <TableRow key={t.id}>
            <TableCell style={{width: "20em"}}>{t.name ?? t.id}</TableCell>
            <TableCell><code>{t.id}</code></TableCell>
            <TableCell>{t.description}</TableCell>
          </TableRow>)
        }
      </TableBody>
    </Table></div>
  </div>
}