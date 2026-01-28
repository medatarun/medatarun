import {useActionPerformer} from "./ActionPerformerHook.tsx";
import {
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  DialogTrigger,
  Field,
  type FieldProps,
  InfoLabel,
  Input,
  type LabelProps,
  MessageBar,
  tokens
} from "@fluentui/react-components";

import {useState} from "react";
import {ActionOutputBox} from "./ActionOutput.tsx";
import {
  ActionDescriptor,
  type ActionResp,
  type FormDataType,
  type FormFieldType,
  useActionRegistry,
  validateForm
} from "../../business";
import type {ActionPerformerState} from "./ActionPerformer.tsx";
import ReactMarkdown from "react-markdown";
import {combineValidationResults, type ValidationResult} from "@seij/common-validation";
import {Button} from "@seij/common-ui";
import {formDataNormalize} from "../../business/action_form.normalize.ts";


export function ActionPerformerView() {

  // Separate state extraction here, so that when state changes all ActionPerformView is redrawn
  const {state} = useActionPerformer();
  const actionRegistry = useActionRegistry()

  if (state.kind === 'idle') return null;

  const {request} = state; // request.location, request.params
  const action = actionRegistry.findActionOptional(request.actionGroupKey, request.actionKey)
  if (!action) return null

  const defaultFormData: FormDataType = {
    ...state.request.params
  }
  const formFields = createFormFields(action, state.request.params);

  return <ActionPerformerViewLoaded
    state={state}
    action={action}
    defaultFormData={defaultFormData}
    formFields={formFields}/>
}

export function ActionPerformerViewLoaded({state, action, defaultFormData, formFields}: {
  state: ActionPerformerState,
  action: ActionDescriptor,
  defaultFormData: FormDataType,
  formFields: FormFieldType[]
}) {

  const actionRegistry = useActionRegistry()
  const {confirmAction, cancelAction, finishAction} = useActionPerformer();
  const [actionResp, setActionResp] = useState<ActionResp | null>(null)
  const [formData, setFormData] = useState<FormDataType>(defaultFormData)

  const displayExecute = state.kind == "pendingUser" || state.kind == "error"
  const displayCancel = state.kind == "pendingUser" || state.kind == "running" || state.kind == "error"
  const displayFinish = state.kind == "done"

  const validationResults = validateForm({formData, formFields})
  const validationResult = combineValidationResults([...validationResults.values()])
  const valid = validationResult.valid

  const onValidate = async () => {
    const payload = formDataNormalize(action.actionGroupKey, action.key, formData, actionRegistry)
    const output = await confirmAction(payload);
    setActionResp(output)
  };

  const onCancel = () => {
    cancelAction();
  };

  const onFinish = () => {
    finishAction()
  }

  const handleChangeFormFieldInput = (field: FormFieldType, value: unknown) => {
    setFormData({...formData, [field.key]: value})
  }


  return (
    <Dialog open={true}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>{action.title}</DialogTitle>
          <DialogContent>
            <div style={{
              display: "flex",
              flexDirection: "column",
              rowGap: tokens.spacingVerticalM,
              columnGap: tokens.spacingVerticalM,
              marginBottom: tokens.spacingVerticalM,

            }}>
              {action.description && <div>{action.description}</div>}
              {formFields.map(field => (

                <FormFieldInput field={field} value={formData[field.key]}
                                validationResult={validationResults.get(field.key)}
                                onChange={handleChangeFormFieldInput}/>))}

              {state.kind === "error" ? <MessageBar intent="error">{state.error?.toString()}</MessageBar> : null}
              {actionResp ? <ActionOutputBox resp={actionResp}/> : null}
            </div>
          </DialogContent>
        </DialogBody>
        <DialogActions>
          {displayExecute &&
            <Button variant="primary" onClick={onValidate} disabled={!valid}>Execute</Button>
          }
          {displayCancel &&
            <DialogTrigger disableButtonEnhancement>
              <Button variant="secondary" onClick={onCancel}>Cancel</Button>
            </DialogTrigger>
          }
          {displayFinish &&
            <DialogTrigger disableButtonEnhancement>
              <Button variant="primary" onClick={onFinish}>Finish</Button>
            </DialogTrigger>
          }
        </DialogActions>
      </DialogSurface>

    </Dialog>
  );

}

function FormFieldInput({field, value, validationResult, onChange}: {
  field: FormFieldType,
  value: unknown,
  validationResult: ValidationResult | undefined,
  onChange: (field: FormFieldType, value: unknown) => void
}) {
  const valueNormalized = (value === null || value === undefined) ? "" : "" + value
  const validationState: FieldProps["validationState"] =
    validationResult === undefined ? "none"
      : validationResult.valid ? "success"
        : validationResult.severity === "WARNING" ? "warning"
          : "error";
  return <div><Field
    label={{
      // Setting children to a render function allows you to replace the entire slot.
      // The first param is the component for the slot (Label), which we're ignoring to use InfoLabel instead.
      // The second param is the props for the slot, which need to be passed to the InfoLabel.
      children: (_: unknown, slotProps: LabelProps) => (
        <InfoLabel {...slotProps}
                   info={field.description ? <ReactMarkdown>{field.description}</ReactMarkdown> : undefined}>
          {field.title}
        </InfoLabel>
      ),
    }}
    validationState={validationState}
    validationMessage={validationResult?.error}
    required={!field.optional}>
    <Input
      disabled={field.prefilled}
      value={valueNormalized}
      onChange={(_, data) => onChange(field, data.value)}/>
  </Field></div>
}

function createFormFields(action: ActionDescriptor, prefill: Record<string, unknown>) {
  const formFields: FormFieldType[] = []
  action.parameters.forEach(param => {
    const prefilledValue = prefill[param.name]
    const field: FormFieldType = {
      key: param.name,
      title: param.title ?? param.name,
      description: param.description,
      optional: param.optional,
      type: param.type,
      order: param.order,
      prefilled: (prefilledValue !== null && prefilledValue !== undefined)
    }
    formFields.push(field)
  })
  return sortFields(formFields);
}

function sortFields(fields: FormFieldType[]): FormFieldType[] {
  return [...fields].sort((a, b) => a.order - b.order)
}
