import {Table, TableBody, TableCell, TableRow} from "@fluentui/react-components";
import {Markdown} from "../../views/ModelPage.tsx";
import {type AttributeDto, useActionRegistry} from "../../business";
import {ActionMenuButton} from "./TypesTable.tsx";
import {useModelContext} from "./ModelContext.tsx";

export function AttributesTable({entityId, attributes}: {entityId: string, attributes: AttributeDto[] }) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions("entity.attribute")
  return <div>
    <Table size="small">
    <TableBody>{attributes.map(attribute => <TableRow key={attribute.id}>
      <TableCell style={{width: "10em"}}>{attribute.name ?? attribute.id}</TableCell>
      <TableCell>
        <div>
          <Markdown value={attribute.description}/>
        </div>
        <div>
          <code>{attribute.id}</code>
          {" "}
          <code>{attribute.type} {attribute.optional ? "?" : ""}</code>
          {attribute.identifierAttribute ? "🔑" : ""}
        </div>
      </TableCell>
      <TableCell style={{width: "2em"}}>
        <ActionMenuButton
          itemActions={itemActions}
          actionParams={{modelKey: model.id, entityKey: entityId, attributeKey: attribute.id}}
        />
      </TableCell>
    </TableRow>)}</TableBody>
  </Table>
  </div>
}