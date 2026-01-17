import {type Action_registryBiz, type TypeDto, useActionRegistry} from "../../business";
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
  Text,
  tokens
} from "@fluentui/react-components";
import {useModelContext} from "./ModelContext.tsx";

import {Icon} from "@seij/common-ui-icons";
import {useActionPerformer} from "./ActionPerformerHook.tsx";
import {useDetailLevelContext} from "./DetailLevelContext.tsx";


export function TypesTable({types}: { types: TypeDto[] }) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions("type")
  const { isDetailLevelTech } = useDetailLevelContext()
  return <div>
    {types.length == 0 ? <div style={{paddingTop: tokens.spacingVerticalL}}>
      <Text italic>No types in this model.</Text>
    </div> : null}
    <div style={{paddingTop:tokens.spacingVerticalM}}><Table size="small" style={{marginBottom: "1em"}}>
      <TableBody>
        {
          types.map(type => <TableRow key={type.id}>
            <TableCell style={{width: "20em"}}>{type.name ?? type.id}</TableCell>
            { isDetailLevelTech && <TableCell><code>{type.id}</code></TableCell> }
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