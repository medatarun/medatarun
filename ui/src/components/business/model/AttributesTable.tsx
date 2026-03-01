import {
  makeStyles,
  Table,
  TableBody,
  TableCell,
  TableRow,
  tokens,
} from "@fluentui/react-components";
import {
  type ActionUILocation,
  useActionRegistry,
} from "@/business/action_registry";
import type { AttributeDto } from "@/business/model";
import { ActionMenuButton } from "./TypesTable.tsx";
import { useModelContext } from "./ModelContext.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { modelTagScope, Tags } from "@/components/core/Tag.tsx";
import { Markdown } from "@/components/core/Markdown.tsx";
import type { ActionPerformerRequestParams } from "@/components/business/actions/ActionPerformer.tsx";

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
  typeCell: {
    width: "10em",
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
  actionUILocation,
  onClickAttribute,
  actionParamsFactory,
}: {
  attributes: AttributeDto[];
  actionParamsFactory: (attributeId: string) => ActionPerformerRequestParams;
  actionUILocation: ActionUILocation;
  onClickAttribute: (id: string) => void;
}) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const { isDetailLevelTech } = useDetailLevelContext();
  const itemActions = actionRegistry.findActions(actionUILocation);
  const styles = useStyles();
  const handleClickAttribute = (id: string) => {
    onClickAttribute(id);
  };
  return (
    <div>
      <Table>
        <TableBody>
          {attributes.map((attribute) => (
            <TableRow key={attribute.id}>
              <TableCell
                className={styles.titleCell}
                onClick={() => handleClickAttribute(attribute.id)}
              >
                {attribute.optional ? "○" : "●"}{" "}
                {attribute.name ?? attribute.key ?? attribute.id}{" "}
                {isDetailLevelTech && (
                  <span>
                    {" "}
                    (<code>{attribute.key}</code>)
                  </span>
                )}
              </TableCell>

              <TableCell
                className={styles.flags}
                onClick={() => handleClickAttribute(attribute.id)}
              >
                {" "}
                {attribute.identifierAttribute ? "🔑" : ""}
              </TableCell>

              <TableCell
                className={styles.typeCell}
                onClick={() => handleClickAttribute(attribute.id)}
              >
                {model.findTypeNameOrKey(attribute.type)}
                {isDetailLevelTech && (
                  <span>
                    {" "}
                    (<code>{model.findTypeKey(attribute.type)}</code>)
                  </span>
                )}
              </TableCell>

              <TableCell
                className={styles.descriptionCell}
                onClick={() => handleClickAttribute(attribute.id)}
              >
                <div>
                  <Markdown value={attribute.description} />
                </div>
                {attribute.tags.length > 0 && (
                  <Tags tags={attribute.tags} scope={modelTagScope(model.id)} />
                )}
              </TableCell>

              <TableCell className={styles.actionCell}>
                <ActionMenuButton
                  itemActions={itemActions}
                  actionParams={actionParamsFactory(attribute.id)}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
