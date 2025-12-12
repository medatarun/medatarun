import {Table, TableBody, TableCell, TableRow} from "@fluentui/react-components";
import {Markdown} from "../../views/ModelPage.tsx";
import type {AttributeDto} from "../../business/model.tsx";

export function AttributesTable({attributes}: { attributes: AttributeDto[] }) {
  return <Table size="small">
    <TableBody>{attributes.map(a => <TableRow key={a.id}>
      <TableCell style={{width: "10em"}}>{a.name ?? a.id}</TableCell>
      <TableCell>
        <div>
          <Markdown value={a.description}/>
        </div>
        <div>
          <code>{a.id}</code>
          {" "}
          <code>{a.type} {a.optional ? "?" : ""}</code>
          {a.identifierAttribute ? "🔑" : ""}
        </div>
      </TableCell>
    </TableRow>)}</TableBody>
  </Table>
}