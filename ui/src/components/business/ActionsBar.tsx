import {useActionRegistry} from "./ActionsContext.tsx";
import {ActionDescriptor} from "../../business/actionDescriptor.tsx";
import {useActionPerformer} from "./ActionPerformerHook.tsx";
import {Button, ButtonBar} from "@seij/common-ui"


export const ActionsBar = ({location, params = {}}: { location: string, params?: Record<string, string> }) => {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(location)
  return <ButtonBar>{actions.map(it => <ActionButton
    key={it.path}
    location={location}
    action={it}
    params={params}/>)}</ButtonBar>
}

export const ActionButton = ({action, params, location}: {
  location: string,
  action: ActionDescriptor,
  params: Record<string, string>
}) => {

  const {performAction, state} = useActionPerformer();
  const disabled = state.kind !== 'idle';

  const handleClick = async () => {
    try {
      await performAction({
        actionKey: action.key,
        actionGroupKey: action.actionGroupKey,
        location: location,
        params: params,
      })
    } catch (e) {
      // We don't manage errors here
      console.error("Error occurred and had not been property processed by action system", e)
    }

  }
  return <Button variant="secondary" disabled={disabled} onClick={handleClick}>{action.title}</Button>
}