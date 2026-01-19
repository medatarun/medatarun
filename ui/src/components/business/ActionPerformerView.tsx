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
import {Action_registryBiz, type ActionResp, useActionRegistry} from "../../business";
import type {ActionPerformerState} from "./ActionPerformer.tsx";
import ReactMarkdown from "react-markdown";
import {combineValidationResults, invalid, valid, type ValidationResult} from "@seij/common-validation";
import {Button} from "@seij/common-ui";

type FormDataType = Record<string, unknown>;
type FormFieldType = {
  key: string
  title: string
  description: string | null
  optional: boolean,
  type: string
  order: number
  prefilled: boolean
}


export function ActionPerformerView() {

  // Separate state extraction here, so that when state changes all ActionPerformView is redrawn
  const {state} = useActionPerformer();
  const actionRegistry = useActionRegistry()

  if (state.kind === 'idle') return null;

  const {request} = state; // request.location, request.params
  const action = actionRegistry.findAction(request.actionGroupKey, request.actionKey)
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
  action: Action_registryBiz,
  defaultFormData: FormDataType,
  formFields: FormFieldType[]
}) {


  const {confirmAction, cancelAction, finishAction} = useActionPerformer();
  const [actionResp, setActionResp] = useState<ActionResp | null>(null)
  const [formData, setFormData] = useState<FormDataType>(defaultFormData)

  const displayExecute = state.kind == "pendingUser"
  const displayCancel = state.kind == "pendingUser" || state.kind == "running"
  const displayFinish = state.kind == "done" || state.kind == "error"

  const validationResults = validate({formData, formFields})
  const validationResult = combineValidationResults([...validationResults.values()])
  const valid = validationResult.valid

  const onValidate = async () => {
    const formDataNormalized = {...formData}; // We should filter
    const output = await confirmAction(formDataNormalized);
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
      <DialogSurface >
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
      // The second param are the props for the slot, which need to be passed to the InfoLabel.
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

function createFormFields(action: Action_registryBiz, prefill: Record<string, unknown>) {
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

function validate({formData, formFields}: {
  formData: FormDataType,
  formFields: FormFieldType[]
}): Map<string, ValidationResult> {
  const validationResults: Map<string, ValidationResult> = new Map();
  for (const formField of formFields) {
    let result = valid;
    if (formField.type === "String") result = validateString(formField, formData[formField.key])
    else if (formField.type === "List<ActionWithPayload>") result = validateString(formField, formData[formField.key])
    else if (formField.type === "AttributeKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "EntityKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "RelationshipKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "TypeKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "ModelKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "Hashtag") result = validateHashtag(formField, formData[formField.key])
    else if (formField.type === "ModelVersion") result = validateVersion(formField, formData[formField.key])
    //else if (formField.type === "Boolean") result = validateBoolean(formField, formData[formField.key])
    else if (formField.type === "Boolean") result = valid
    else if (formField.type === "LocalizedText") result = validateString(formField, formData[formField.key])
    else if (formField.type === "LocalizedMarkdown") result = validateString(formField, formData[formField.key])
    else if (formField.type === "RelationshipRoleKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "RelationshipCardinality") result = validateString(formField, formData[formField.key])
    else result = invalid("Unsupported type: " + formField.type)
    validationResults.set(formField.key, result)
  }
  return validationResults
}

function validateKey(field: FormFieldType, formDatum: any) {
  const valid = validateString(field, formDatum)
  if (!valid.valid) return valid
  if (valid.valid && (formDatum === null || formDatum === undefined || formDatum === "")) return valid
  if (formDatum.length > 255) return invalid("Too long")
  if (formDatum.length < 1) return invalid("Too short")
  return valid
}

/*
function validateBoolean(field: FormFieldType, formDatum: any) {
  if (formDatum === null || formDatum === undefined) {
    if (field.optional) return valid
    else return invalid("Required")
  }
  if (typeof formDatum !== "boolean") return invalid("Must be a boolean")
  else return valid
}
*/
function validateString(field: FormFieldType, formDatum: any) {
  if (formDatum === null || formDatum === undefined) {
    if (field.optional) return valid
    else return invalid("Required")
  }
  if (typeof formDatum !== "string") return invalid("Must be a string")
  if (formDatum.length === 0) {
    if (field.optional) return valid
    else return invalid("Required")
  }
  return valid
}

function validateVersion(field: FormFieldType, formDatum: any) {
  return validateString(field, formDatum)
}

function validateHashtag(field: FormFieldType, formDatum: any) {
  return validateString(field, formDatum)
}