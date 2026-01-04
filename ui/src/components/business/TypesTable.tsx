import type {Action_registryBiz, TypeDto} from "../../business";
import {
  Button,
  Menu,
  MenuItem,
  MenuList,
  MenuPopover,
  MenuTrigger,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text
} from "@fluentui/react-components";
import {ActionsBar} from "./ActionsBar.tsx";
import {useModelContext} from "./ModelContext.tsx";

import {Icon} from "@seij/common-ui-icons";
import {useActionRegistry} from "./ActionsContext.tsx";
import {useActionPerformer} from "./ActionPerformerHook.tsx";


export function TypesTable({types}: { types: TypeDto[] }) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions("type")
  itemActions.push(...actionRegistry.findActions("type.name"))
  itemActions.push(...actionRegistry.findActions("type.description"))

  return <div>
    <div>
      <ActionsBar location="model.types" params={{modelKey: model.id}}/>
    </div>
    <div>
      {types.length == 0 ? <Text italic>No types found</Text> : null}
    </div>
    <div><Table size="small" style={{marginBottom: "1em"}}>
      <TableBody>
        {
          types.map(type => <TableRow key={type.id}>
            <TableCell style={{width: "20em"}}>{type.name ?? type.id}</TableCell>
            <TableCell><code>{type.id}</code></TableCell>
            <TableCell>{type.description}</TableCell>
            <TableCell style={{width: "2em"}}>
              <ActionMenuButton
                itemActions={itemActions}
                actionParams={{modelKey: model.id, typeKey: type.id}}
              />
            </TableCell>
          </TableRow>)
        }
      </TableBody>
    </Table></div>
  </div>
}

export function ActionMenuButton({itemActions, actionParams}: {
  itemActions: Action_registryBiz[],
  actionParams: Record<string, string>
}) {
  const actionPerformer = useActionPerformer()
  return <Menu positioning={{autoSize: true}}>
    <MenuTrigger disableButtonEnhancement>
      <Button icon={<Icon name="more_menu_vertical"/>}/>
    </MenuTrigger>
    <MenuPopover>
      <MenuList>
        {itemActions.map((action) => (
          <MenuItem onClick={() => actionPerformer.performAction({
            actionKey: action.key,
            actionGroupKey: action.actionGroupKey,
            params: actionParams
          })} icon={undefined}>
            {action.title}
          </MenuItem>
        ))}
      </MenuList>
    </MenuPopover>
  </Menu>
}