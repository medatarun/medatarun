import {useActionRegistry} from "./ActionsContext.tsx";
import {Button, makeStyles, tokens} from "@fluentui/react-components";
import {ActionDescriptor} from "../../business/actionDescriptor.tsx";

const useStyles = makeStyles({
  root: {
    display: "flex",
    columnGap: tokens.spacingHorizontalS,
  }
})

export const ActionsBar = ({location}: { location: string }) => {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(location)
  const styles = useStyles()
  return <div className={styles.root}>{actions.map(it => <ActionButton key={it.path} action={it}/>)}</div>


}

export const ActionButton = ({action}: { action: ActionDescriptor }) => {
  const handleClick = () => {}
  return <Button appearance="secondary" onClick={handleClick}>{action.title}</Button>
}