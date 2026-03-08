import {
  type ActionDescriptor,
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import type { TypeDto } from "@/business/model";
import {
  Button,
  makeStyles,
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
  tokens,
} from "@fluentui/react-components";
import { useModelContext } from "./ModelContext.tsx";

import { Icon } from "@seij/common-ui-icons";
import { useActionPerformer } from "@/components/business/actions/ActionPerformerHook.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { Markdown } from "@/components/core/Markdown.tsx";
import {
  createActionTemplateType, createDisplayedSubjectModel,
  createDisplayedSubjectType,
} from "./model.actions.ts";
import type { ActionDisplayedSubject, ActionPerformerRequestParams } from "@/components/business/actions/ActionPerformer.tsx";
import { displaySubjectNone } from "@/components/business/actions/ActionPerformer.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";

const useStyles = makeStyles({
  titleCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "20rem",
    verticalAlign: "baseline",
    wordBreak: "break-all",
  },
  flags: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "1em",
    verticalAlign: "baseline",
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    "& p": {
      marginTop: 0,
    },
    "& p:last-child": {
      marginBottom: 0,
    },
  },
  typeCodeCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    width: "10rem",
  },
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right",
  },
});

export function TypesTable({
  types,
  onClick,
}: {
  types: TypeDto[];
  onClick: (typeId: string) => void;
}) {
  const { t } = useAppI18n();
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions(ActionUILocations.type);
  const { isDetailLevelTech } = useDetailLevelContext();
  const styles = useStyles();
  const displayedSubject = createDisplayedSubjectModel(model.id);

  return (
    <div>
      {types.length == 0 ? (
        <p style={{ paddingTop: tokens.spacingVerticalL }}>
          <Text italic>{t("typesTable_empty")}</Text>
        </p>
      ) : null}
      <div style={{ paddingTop: tokens.spacingVerticalM }}>
        <Table size="small" style={{ marginBottom: "1em" }}>
          <TableBody>
            {types.map((type) => {
              return (
                <TableRow key={type.id}>
                  <TableCell
                    className={styles.titleCell}
                    onClick={() => onClick(type.id)}
                  >
                    {model.findTypeNameOrKey(type.id)}
                  </TableCell>

                  <TableCell
                    className={styles.flags}
                    onClick={() => onClick(type.id)}
                  >
                    {" "}
                  </TableCell>

                  {isDetailLevelTech && (
                    <TableCell
                      className={styles.typeCodeCell}
                      onClick={() => onClick(type.id)}
                    >
                      <code>{type.key}</code>
                    </TableCell>
                  )}
                  <TableCell
                    className={styles.descriptionCell}
                    onClick={() => onClick(type.id)}
                  >
                    <div>
                      <Markdown value={type.description}/>
                    </div>
                  </TableCell>

                  <TableCell className={styles.actionCell}>
                    <ActionMenuButton
                      itemActions={itemActions}
                      actionParams={createActionTemplateType(model.id, type.id)}
                      displayedSubject={displayedSubject}
                    />
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}

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
              onClick={() =>
                actionPerformer.performAction({
                  actionKey: action.key,
                  actionGroupKey: action.actionGroupKey,
                  params: actionParams,
                  displayedSubject: displayedSubject ?? displaySubjectNone,
                })
              }
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
