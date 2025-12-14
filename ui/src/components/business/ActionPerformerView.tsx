import {useActionPerformer} from "./ActionPerformerHook.tsx";
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  DialogTrigger,
  MessageBar
} from "@fluentui/react-components";
import {useActionRegistry} from "./ActionsContext.tsx";
import {useState} from "react";
import {ActionOutputBox} from "./ActionOutput.tsx";
import type {ActionResp} from "../../business/actionDescriptor.tsx";
import type {ActionPerformerState} from "./ActionPerformer.tsx";

export function ActionPerformerView() {
  // Separate state extraction here, so that when state changes all ActionPerformView is redrawn
  const {state} = useActionPerformer();
  if (state.kind === 'idle') return null;
  return <ActionPerformerViewLoaded state={state} />
}

export function ActionPerformerViewLoaded({state}:{state:ActionPerformerState}) {

  const actionRegistry = useActionRegistry()
  const { confirmAction, cancelAction, finishAction} = useActionPerformer();
  const [actionOutput, setActionOutput] = useState<ActionResp|null>(null)

  if (state.kind === 'idle') return null;


  const {request} = state; // request.location, request.params
  const action = actionRegistry.findAction(request.actionGroupKey, request.actionKey)

  if (!action) return null

  const displayExecute = state.kind == "pendingUser"
  const displayCancel = state.kind == "pendingUser" || state.kind == "running"
  const displayFinish = state.kind == "done" || state.kind == "error"

  const onValidate = async () => {
    const formData = {...request.params}; // normalement issu de ton formulaire
    const output  = await confirmAction(formData);
    setActionOutput(output)
  };



  const onCancel = () => {
    cancelAction();
  };

  const onFinish = () => {
    finishAction()
  }


  return (
    <Dialog open={true}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>{action.title}</DialogTitle>
          <DialogContent>
            <div>Location: {request.location} <code>{JSON.stringify(request.params)}</code></div>
            <MessageBar>{state.kind}</MessageBar>
            { state.kind === "error" ? <MessageBar intent="error">{state.error?.toString()}</MessageBar> : null }
            { actionOutput ? <ActionOutputBox resp={actionOutput} /> : null }
          </DialogContent>
        </DialogBody>
        <DialogActions>
          {displayExecute &&
            <Button type="button" appearance="primary" onClick={onValidate}>Execute</Button>
          }
          {displayCancel &&
            <DialogTrigger disableButtonEnhancement>
              <Button appearance="secondary" onClick={onCancel}>Cancel</Button>
            </DialogTrigger>
          }
          {displayFinish &&
            <DialogTrigger disableButtonEnhancement>
              <Button appearance="primary" onClick={onFinish}>Finish</Button>
            </DialogTrigger>
          }
        </DialogActions>
      </DialogSurface>

    </Dialog>
  );

}