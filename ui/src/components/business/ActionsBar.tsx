import {Action_registryBiz, useActionRegistry} from "../../business";
import {useActionPerformer} from "./ActionPerformerHook.tsx";
import {Button, ButtonBar} from "@seij/common-ui"
import type {ComponentProps} from "react";

type ActionBarProps = {
  location: string,
  params?: Record<string, string>,
  variant?: ComponentProps<typeof ButtonBar>["variant"]
}

export const ActionsBar = ({location, params = {}, variant}: ActionBarProps) => {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(location)
  return <ButtonBar variant={variant}>{actions.map(it => <ActionButton
    key={it.path}
    location={location}
    action={it}
    params={params}/>)}</ButtonBar>
}

export const ActionButton = ({action, params}: {
  location: string,
  action: Action_registryBiz,
  params: Record<string, string>
}) => {

  const {performAction, state} = useActionPerformer();
  const disabled = state.kind !== 'idle';

  const handleClick = async () => {
    try {
      await performAction({
        actionKey: action.key,
        actionGroupKey: action.actionGroupKey,
        params: params,
      })
    } catch (e) {
      // We don't manage errors here
      console.error("Error occurred and had not been property processed by action system", e)
    }

  }
  return <Button variant="secondary" disabled={disabled} onClick={handleClick}>{action.title}</Button>
}