import {makeStyles, Table, TableBody, TableCell, TableRow, tokens} from "@fluentui/react-components";
import {Markdown} from "../../views/ModelPage.tsx";
import {type AttributeDto, useActionRegistry} from "../../business";
import {ActionMenuButton} from "./TypesTable.tsx";
import {useModelContext} from "./ModelContext.tsx";
import {useDetailLevelContext} from "./DetailLevelContext.tsx";
import {Tags} from "../core/Tag.tsx";

const useStyles = makeStyles({
  titleCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "18em", verticalAlign: "baseline", whiteSpace: "nowrap"
  },
  flags: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "1em", verticalAlign: "baseline"
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    "& p": {
      marginTop: 0
    }
  }
})

export function AttributesTable({entityId, attributes}: { entityId: string, attributes: AttributeDto[] }) {
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const {isDetailLevelTech} = useDetailLevelContext()
  const itemActions = actionRegistry.findActions("entity.attribute")
  const styles = useStyles()

  return <div>
    <Table >
      <TableBody>{attributes.map(attribute =>
        <TableRow key={attribute.id}>
          <TableCell className={styles.titleCell}>{attribute.name ?? attribute.id}</TableCell>
          <TableCell className={styles.flags}> {attribute.identifierAttribute ? "🔑" : ""}</TableCell>
          <TableCell className={styles.descriptionCell}>
            <div>
              <Markdown value={attribute.description}/>
            </div>
            <div>{model.findTypeName(attribute.type)}</div>
            {isDetailLevelTech && <div>
              <code>{attribute.id}</code>
              {" "}
              <code>{attribute.type} {attribute.optional ? "?" : ""}</code>
            </div>}
            { attribute.hashtags.length > 0 &&  <Tags tags={attribute.hashtags}/> }
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