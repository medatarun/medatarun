import {makeStyles, Table, TableBody, TableCell, TableRow, tokens} from "@fluentui/react-components";
import {ActionUILocations, type AttributeDto, useActionRegistry} from "../../business";
import {ActionMenuButton} from "./TypesTable.tsx";
import {useModelContext} from "./ModelContext.tsx";
import {useDetailLevelContext} from "./DetailLevelContext.tsx";
import {Tags} from "../core/Tag.tsx";
import {Markdown} from "../core/Markdown.tsx";

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

export function AttributesTable({entityId, attributes, onClickAttribute}: {
  entityId: string,
  attributes: AttributeDto[],
  onClickAttribute: (id: string) => void
}) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const {isDetailLevelTech} = useDetailLevelContext()
  const itemActions = actionRegistry.findActions(ActionUILocations.entity_attribute)
  const styles = useStyles()
  const handleClickAttribute = (id: string) => {
    onClickAttribute(id)
  }
  return <div>
    <Table>
      <TableBody>{attributes.map(attribute =>
        <TableRow key={attribute.id}>
          <TableCell className={styles.titleCell} onClick={()=>handleClickAttribute(attribute.id)}>{attribute.name ?? attribute.id} </TableCell>
          <TableCell className={styles.flags} onClick={()=>handleClickAttribute(attribute.id)}> {attribute.identifierAttribute ? "🔑" : ""}</TableCell>
          <TableCell className={styles.descriptionCell} onClick={()=>handleClickAttribute(attribute.id)}>
            <div>
              <Markdown value={attribute.description}/>
            </div>
            <div>{model.findTypeName(attribute.type)}</div>
            {isDetailLevelTech && <div>
              <code>{attribute.id}</code>
              {" "}
              <code>{attribute.type} {attribute.optional ? "?" : ""}</code>
            </div>}
            {attribute.hashtags.length > 0 && <Tags tags={attribute.hashtags}/>}
          </TableCell>
          <TableCell className={styles.actionCell}>
            <ActionMenuButton
              itemActions={itemActions}
              actionParams={{modelKey: model.id, entityKey: entityId, attributeKey: attribute.id}}
            />
          </TableCell>
        </TableRow>)}</TableBody>
    </Table>
  </div>
}