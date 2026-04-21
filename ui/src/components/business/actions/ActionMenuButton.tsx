import type { ActionDescriptor } from "@/business/action_registry";
import { type ActionCtx } from "@/components/business/actions";
import { useActionPerformer } from "@/components/business/actions/ActionPerformerHook.tsx";
import {
  Button,
  Menu,
  MenuItem,
  MenuList,
  MenuPopover,
  MenuTrigger,
} from "@fluentui/react-components";
import { Icon } from "@seij/common-ui-icons";
import { useSecurityContext } from "@/business/security";

export function ActionMenuButton({
  itemActions,
  actionCtx,
  label,
}: {
  label?: string;
  itemActions: ActionDescriptor[];
  actionCtx: ActionCtx;
}) {
  const actionPerformer = useActionPerformer();
  const { canExecute } = useSecurityContext();
  const itemActionsSafe = itemActions.filter((it) =>
    canExecute(it.securityRule),
  );
  if (itemActionsSafe.length === 0) return null;
  return (
    <Menu positioning={{ autoSize: true }}>
      <MenuTrigger disableButtonEnhancement>
        <Button iconPosition="after" icon={<Icon name="more_menu_vertical" />}>
          {label}
        </Button>
      </MenuTrigger>
      <MenuPopover>
        <MenuList>
          {itemActionsSafe.map((action) => (
            <MenuItem
              key={action.key}
              onClick={() => {
                actionPerformer.performAction({
                  actionKey: action.key,
                  actionGroupKey: action.actionGroupKey,
                  ctx: actionCtx,
                });
              }}
              icon={undefined}
            >
              {action.title}
            </MenuItem>
          ))}
        </MenuList>
      </MenuPopover>
    </Menu>
  );
}
