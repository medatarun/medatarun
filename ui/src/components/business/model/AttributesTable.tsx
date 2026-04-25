import {
  Caption1,
  makeStyles,
  Table,
  TableBody,
  TableCell,
  TableRow,
  tokens,
  Tooltip,
} from "@fluentui/react-components";
import type { AttributeDto } from "@/business/model";
import { useModelContext } from "./ModelContext.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { modelTagScope, TagsCondensed } from "@/components/core/Tag.tsx";
import type { ActionCtx } from "@/business/action-performer";
import { Key } from "@/components/core/Key.tsx";
import { MarkdownSummary } from "@/components/core/MarkdownSummary.tsx";
import {
  KeyRegular,
  SquareFilled,
  SquareHintRegular,
} from "@fluentui/react-icons";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import type { ActionKey } from "@/business/action_registry/actionRegistry.dictionnary.ts";
import { useActionRegistry } from "@/components/business/actions";

const useStyles = makeStyles({
  titleCell: {
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
  },
  typeCell: {
    width: "20em",
    verticalAlign: "baseline",
    textAlign: "right",
  },
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right",
  },
});

export function AttributesTable({
  attributes,
  onClickAttribute,
  parentId,
  actionCtxAttribute,
  actions,
}: {
  attributes: AttributeDto[];
  parentId: string;
  onClickAttribute: (id: string) => void;
  actionCtxAttribute: (attr: AttributeDto) => ActionCtx;
  actions: ActionKey[];
}) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const { isDetailLevelTech } = useDetailLevelContext();
  const itemActions = actionRegistry.findActionDescriptors(actions);
  const styles = useStyles();
  const handleClickAttribute = (id: string) => {
    onClickAttribute(id);
  };
  return (
    <div>
      <Table>
        <TableBody>
          {attributes.map((attribute) => (
            <TableRow
              key={attribute.id}
              style={{ border: "1px solid " + tokens.colorNeutralStroke2 }}
            >
              <TableCell
                className={styles.titleCell}
                onClick={() => handleClickAttribute(attribute.id)}
              >
                <div>
                  {attribute.name ?? (
                    <Key value={attribute.key ?? attribute.id} />
                  )}
                </div>
                <div>
                  {attribute.name && isDetailLevelTech && (
                    <Key value={attribute.key} />
                  )}
                </div>
                <div>
                  <Caption1>
                    <MarkdownSummary
                      value={attribute.description}
                      maxChars={200}
                    />
                  </Caption1>
                </div>
                {attribute.tags.length > 0 && (
                  <div style={{ marginTop: tokens.spacingVerticalM }}>
                    <TagsCondensed
                      tags={attribute.tags}
                      scope={modelTagScope(model.id)}
                    />
                  </div>
                )}
              </TableCell>

              <TableCell
                className={styles.typeCell}
                onClick={() => handleClickAttribute(attribute.id)}
              >
                <div
                  style={{
                    display: "inline-flex",
                    columnGap: tokens.spacingVerticalS,
                    alignContent: "baseline",
                  }}
                >
                  <div>
                    {model.isEntityAttributePK(parentId, attribute.id) ? (
                      <KeyRegular />
                    ) : (
                      ""
                    )}{" "}
                  </div>
                  <div>{model.findTypeNameOrKey(attribute.type)}</div>
                  <div style={{ width: "1em", verticalAlign: "middle" }}>
                    {attribute.optional ? (
                      <Tooltip content={"Obligatoire"} relationship={"label"}>
                        <SquareHintRegular />
                      </Tooltip>
                    ) : (
                      <Tooltip content={"Optionel"} relationship={"label"}>
                        <SquareFilled />
                      </Tooltip>
                    )}
                  </div>
                </div>
                {isDetailLevelTech && (
                  <div>
                    <Key value={model.findTypeKey(attribute.type) ?? ""} />
                  </div>
                )}
              </TableCell>

              <TableCell className={styles.actionCell}>
                <ActionMenuButton
                  itemActions={itemActions}
                  actionCtx={actionCtxAttribute(attribute)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
