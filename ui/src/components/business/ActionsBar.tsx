import {useActionRegistry} from "./ActionsContext.tsx";
import {Button, makeStyles, tokens} from "@fluentui/react-components";
import {ActionDescriptor} from "../../business/actionDescriptor.tsx";
import {useActionPerformer} from "./ActionPerformerHook.tsx";

const useStyles = makeStyles({
  root: {
    display: "flex",
    columnGap: tokens.spacingHorizontalS,
  }
})

export const ActionsBar = ({location, params = {}}: { location: string, params?: Record<string, string> }) => {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(location)
  const styles = useStyles()
  return <div className={styles.root}>{actions.map(it => <ActionButton key={it.path} action={it}
                                                                       params={params}/>)}</div>
}

export const ActionButton = ({action, params}: { action: ActionDescriptor, params: Record<string, string> }) => {

  const {performAction, state} = useActionPerformer();
  const disabled = state.kind !== 'idle';

  const handleClick = async () => {
    try {
      await performAction(action.key, params)
    } catch(e) {
      // We don't manage errors here
      console.error("Error occurred and had not been property processed by action system", e)
    }

  }
  return <Button appearance="secondary" disabled={disabled} onClick={handleClick}>{action.title}</Button>
}