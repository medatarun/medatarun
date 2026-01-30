import {makeStyles, Table, TableBody, TableCell, TableRow, tokens} from "@fluentui/react-components";
import {type ActionUILocation, type AttributeDto, useActionRegistry} from "../../business";
import {ActionMenuButton} from "./TypesTable.tsx";
import {useModelContext} from "./ModelContext.tsx";
import {useDetailLevelContext} from "./DetailLevelContext.tsx";
import {Tags} from "../core/Tag.tsx";
import {Markdown} from "../core/Markdown.tsx";
import type {ActionPerformerRequestParams} from "./ActionPerformer.tsx";

const useStyles = makeStyles({
  titleCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "20rem",
    verticalAlign: "baseline",
    wordBreak: "break-all"
  },
  flags: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "1em", verticalAlign: "baseline"
  },
  typeCell: {
    width: "10em", verticalAlign: "baseline"
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    "& p": {
      marginTop: 0
    },
    "& p:last-child": {
      marginBottom: 0
    }
  },
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right"

  }
})

export function AttributesTable({attributes, actionUILocation, onClickAttribute, actionParamsFactory}: {
  attributes: AttributeDto[],
  actionParamsFactory: (attributeId: string) => ActionPerformerRequestParams,
  actionUILocation: ActionUILocation,
  onClickAttribute: (id: string) => void
}) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const {isDetailLevelTech} = useDetailLevelContext()
  const itemActions = actionRegistry.findActions(actionUILocation)
  const styles = useStyles()
  const handleClickAttribute = (id: string) => {
    onClickAttribute(id)
  }
  return <div>
    <Table>
      <TableBody>{attributes.map(attribute =>
        <TableRow key={attribute.id}>

          <TableCell
            className={styles.titleCell}
            onClick={() => handleClickAttribute(attribute.id)}>{attribute.optional ? "○" : "●"} {attribute.name ?? attribute.key ?? attribute.id} {isDetailLevelTech &&
            <span>{" "}(<code>{attribute.key}</code>)</span>}</TableCell>

          <TableCell
            className={styles.flags}
            onClick={() => handleClickAttribute(attribute.id)}> {attribute.identifierAttribute ? "🔑" : ""}</TableCell>

          <TableCell
            className={styles.typeCell}
            onClick={() => handleClickAttribute(attribute.id)}>
            {model.findTypeNameOrKey(attribute.type)}
            {isDetailLevelTech && <span>{" "}(<code>{model.findTypeKey(attribute.type)}</code>)</span>}
          </TableCell>

          <TableCell
            className={styles.descriptionCell}
            onClick={() => handleClickAttribute(attribute.id)}>
            <div>
              <Markdown value={attribute.description}/>
            </div>
            {attribute.hashtags.length > 0 && <Tags tags={attribute.hashtags}/>}
          </TableCell>

          <TableCell className={styles.actionCell}>
            <ActionMenuButton
              itemActions={itemActions}
              actionParams={actionParamsFactory(attribute.id)}
            />
          </TableCell>
        </TableRow>)}</TableBody>
    </Table>
  </div>
}