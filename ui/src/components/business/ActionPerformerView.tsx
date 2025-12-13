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

export function ActionPerformerView() {

  const {state, confirmAction, cancelAction, finishAction} = useActionPerformer();

  if (state.kind === 'idle') return null;

  const {request} = state; // request.location, request.params


  const displayExecute = state.kind == "pendingUser"
  const displayCancel = state.kind == "pendingUser" || state.kind == "error" || state.kind == "running"
  const displayFinish = state.kind == "done"

  const onValidate = async () => {
    const formData = {foo: 'bar'}; // normalement issu de ton formulaire
    await confirmAction(formData);
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
          <DialogTitle>Action</DialogTitle>
          <DialogContent>
            <div>Action demandée sur {request.location}</div>
            <MessageBar>{state.kind}</MessageBar>
            <pre>{JSON.stringify(request.params)}</pre>
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