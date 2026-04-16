import type { ActionDescriptor } from "@/business/action_registry";
import {
  type ActionDisplayedSubject,
  type ActionPerformerRequestParams,
  displaySubjectNone,
} from "@/components/business/actions";
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

export function ActionMenuButton({
  itemActions,
  actionParams,
  displayedSubject,
  label,
}: {
  label?: string;
  itemActions: ActionDescriptor[];
  actionParams: ActionPerformerRequestParams;
  /**
   * Page subject propagated to action performer.
   * Keep it equal to the page displayed subject.
   */
  displayedSubject: ActionDisplayedSubject;
}) {
  const actionPerformer = useActionPerformer();
  if (itemActions.length === 0) return null;
  return (
    <Menu positioning={{ autoSize: true }}>
      <MenuTrigger disableButtonEnhancement>
        <Button iconPosition="after" icon={<Icon name="more_menu_vertical" />}>
          {label}
        </Button>
      </MenuTrigger>
      <MenuPopover>
        <MenuList>
          {itemActions.map((action) => (
            <MenuItem
              onClick={() => {
                actionPerformer.performAction({
                  actionKey: action.key,
                  actionGroupKey: action.actionGroupKey,
                  params: actionParams,
                  displayedSubject: displayedSubject ?? displaySubjectNone,
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
