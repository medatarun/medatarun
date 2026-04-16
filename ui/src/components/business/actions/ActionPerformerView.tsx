import { useActionPerformer } from "./ActionPerformerHook.tsx";
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
  type LabelProps,
  tokens,
} from "@fluentui/react-components";

import { type Ref, useEffect, useRef, useState } from "react";
import { ActionOutputBox } from "./ActionOutput.tsx";
import { type ActionResp } from "@/business/action_runner";
import {
  type FormDataType,
  type FormFieldType,
  validateForm,
} from "@/business/action_form";
import {
  ActionDescriptor,
  useActionRegistry,
} from "@/business/action_registry";
import type {
  ActionPerformerRequest,
  ActionPerformerRequestParams,
} from "./ActionPerformerRequest.tsx";
import ReactMarkdown from "react-markdown";
import {
  combineValidationResults,
  type ValidationResult,
} from "@seij/common-validation";
import { Button, ErrorBox } from "@seij/common-ui";
import { formDataNormalize } from "@/business/action_form/action_form.normalize.ts";
import { isNil, isPlainObject } from "lodash-es";
import { toProblem } from "@seij/common-types";
import { useAppI18n } from "@/services/appI18n.tsx";
import { useNavigate } from "@tanstack/react-router";
import type { ActionPerformerInputProps } from "./inputs/ActionPerformerInputProps.tsx";
import { ActionPerformerInputList } from "./inputs/ActionPerformerInputList.tsx";
import { TypeRegistryInstance } from "@/business/types/TypeRegistry.ts";
import {
  ACTION_PERFORMER_INPUT_COMPONENTS_BY_TYPE,
  ACTION_PERFORMER_INPUT_DEFAULT_COMPONENT,
} from "./inputs/ActionPerformerInputRegistry.ts";
import type { ActionPerformerState } from "./ActionPerformer.tsx";

const DEBUG = false;

export function ActionPerformerView() {
  // Separate state extraction here, so that when state changes all ActionPerformView is redrawn
  const { state } = useActionPerformer();
  const actionRegistry = useActionRegistry();

  if (state.kind === "idle") return null;

  const { request } = state; // request.location, request.params
  const action = actionRegistry.findActionOptional(
    request.actionGroupKey,
    request.actionKey,
  );
  if (!action) return null;

  const defaultFormData: FormDataType = {};
  for (const actionParam of action.parameters) {
    defaultFormData[actionParam.name] =
      state.request.params[actionParam.name]?.value ?? null;
  }

  const formFields = createFormFields(action, state.request.params);

  return (
    <ActionPerformerViewLoaded
      request={request}
      state={state}
      action={action}
      defaultFormData={defaultFormData}
      formFields={formFields}
    />
  );
}

export function ActionPerformerViewLoaded({
  request,
  state,
  action,
  defaultFormData,
  formFields,
}: {
  request: ActionPerformerRequest;
  state: ActionPerformerState;
  action: ActionDescriptor;
  defaultFormData: FormDataType;
  formFields: FormFieldType[];
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const { confirmAction, cancelAction, finishAction, postHooks } =
    useActionPerformer();
  const navigate = useNavigate();
  const [actionResp, setActionResp] = useState<ActionResp | null>(null);
  const [formData, setFormData] = useState<FormDataType>(defaultFormData);
  const firstInputRef = useRef<HTMLInputElement>(null);

  const displayExecute = state.kind == "pendingUser" || state.kind == "error";
  const displayCancel =
    state.kind == "pendingUser" ||
    state.kind == "running" ||
    state.kind == "error";
  const displayFinish = state.kind == "done";

  const validationResults = validateForm({ formData, formFields });
  const validationResult = combineValidationResults([
    ...validationResults.values(),
  ]);
  const valid = validationResult.valid;

  const onValidate = async () => {
    const payload = formDataNormalize(
      action.actionGroupKey,
      action.key,
      formData,
      actionRegistry,
    );
    const output = await confirmAction(payload);
    setActionResp(output);
  };

  const onCancel = () => {
    cancelAction();
  };

  const onFinish = () => {
    finishAction();
  };

  const handleChangeFormFieldInput = (field: FormFieldType, value: unknown) => {
    setFormData({ ...formData, [field.key]: value });
  };

  useEffect(() => {
    const isDone = state.kind === "done";
    const display = hasResultToDisplay(actionResp);
    if (isDone && !display) {
      finishAction();
    }
  }, [state.kind, actionResp, finishAction]);

  useEffect(() => {
    if (state.kind !== "done") return;

    postHooks.resolveNavigationAfterSuccess({
      action: action,
      request: state.request,
      state: state,
      navigate: navigate,
    });
  }, [action, navigate, postHooks, state]);

  const focusedFieldKey = formFields.find(
    (it) => it.visible && !it.readonly,
  )?.key;

  useEffect(() => {
    firstInputRef?.current?.focus();
  }, [focusedFieldKey, action.key]);

  return (
    <Dialog open={true}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>{action.title}</DialogTitle>
          <DialogContent>
            <div
              style={{
                display: "flex",
                flexDirection: "column",
                rowGap: tokens.spacingVerticalM,
                columnGap: tokens.spacingVerticalM,
                marginBottom: tokens.spacingVerticalM,
              }}
            >
              {action.description && <div>{action.description}</div>}
              {formFields
                .filter((it) => it.visible)
                .map((field) => (
                  <FormFieldInput
                    key={field.key}
                    inputRef={
                      field.key === focusedFieldKey ? firstInputRef : undefined
                    }
                    request={request}
                    field={field}
                    value={formData[field.key]}
                    validationResult={validationResults.get(field.key)}
                    onChange={handleChangeFormFieldInput}
                  />
                ))}

              {state.kind === "error" ? (
                <ErrorBox error={toProblem(state.error)} />
              ) : null}
              {actionResp ? <ActionOutputBox resp={actionResp} /> : null}
              {DEBUG && <pre>{JSON.stringify(formData, null, 2)}</pre>}
            </div>
          </DialogContent>
        </DialogBody>
        <DialogActions>
          {displayExecute && (
            <Button variant="primary" onClick={onValidate} disabled={!valid}>
              {t("actionPerformerView_execute")}
            </Button>
          )}
          {displayCancel && (
            <DialogTrigger disableButtonEnhancement>
              <Button variant="secondary" onClick={onCancel}>
                {t("actionPerformerView_cancel")}
              </Button>
            </DialogTrigger>
          )}
          {displayFinish && (
            <DialogTrigger disableButtonEnhancement>
              <Button variant="primary" onClick={onFinish}>
                {t("actionPerformerView_finish")}
              </Button>
            </DialogTrigger>
          )}
        </DialogActions>
      </DialogSurface>
    </Dialog>
  );
}

function FormFieldInput({
  request,
  field,
  value,
  validationResult,
  inputRef,
  onChange,
}: {
  request: ActionPerformerRequest;
  field: FormFieldType;
  value: unknown;
  validationResult: ValidationResult | undefined;
  onChange: (field: FormFieldType, value: unknown) => void;
  inputRef?: Ref<HTMLInputElement>;
}) {
  const valueNormalized = value === null || value === undefined ? null : value;
  const validationState: FieldProps["validationState"] =
    validationResult === undefined
      ? "none"
      : validationResult.valid
        ? "success"
        : validationResult.severity === "WARNING"
          ? "warning"
          : "error";
  const disabled = field.readonly;
  const inputProps: ActionPerformerInputProps = {
    request: request,
    inputRef: inputRef,
    value: valueNormalized,
    disabled: disabled,
    onValueChange: (nextValue) => onChange(field, nextValue),
  };

  const fieldType = TypeRegistryInstance.typeDecode(field.type);
  const InputComponent =
    ACTION_PERFORMER_INPUT_COMPONENTS_BY_TYPE[fieldType.type] ??
    ACTION_PERFORMER_INPUT_DEFAULT_COMPONENT;

  return (
    <div>
      <Field
        label={{
          // Setting children to a render function allows you to replace the entire slot.
          // The first param is the component for the slot (Label), which we're ignoring to use InfoLabel instead.
          // The second param is the props for the slot, which need to be passed to the InfoLabel.
          children: (_: unknown, slotProps: LabelProps) => (
            <InfoLabel
              {...slotProps}
              info={
                field.description ? (
                  <ReactMarkdown>{field.description}</ReactMarkdown>
                ) : undefined
              }
            >
              {field.title}
            </InfoLabel>
          ),
        }}
        validationState={validationState}
        validationMessage={validationResult?.error}
        required={!field.optional}
      >
        {fieldType.isList ? (
          <ActionPerformerInputList
            {...(inputProps as ActionPerformerInputProps<unknown[]>)}
            inputType={fieldType.type}
          />
        ) : (
          <InputComponent {...inputProps} />
        )}
      </Field>
    </div>
  );
}

function createFormFields(
  action: ActionDescriptor,
  prefill: ActionPerformerRequestParams,
) {
  const formFields: FormFieldType[] = [];
  action.parameters.forEach((param) => {
    const prefilledValue = prefill[param.name];
    const field: FormFieldType = {
      key: param.name,
      title: param.title ?? param.name,
      description: param.description,
      optional: param.optional,
      type: param.type,
      order: param.order,
      readonly: isNil(prefilledValue) ? false : prefilledValue.readonly,
      visible: isNil(prefilledValue) ? true : prefilledValue.visible,
    };
    formFields.push(field);
  });
  return sortFields(formFields);
}

function sortFields(fields: FormFieldType[]): FormFieldType[] {
  return [...fields].sort((a, b) => a.order - b.order);
}

function hasResultToDisplay(actionResp: ActionResp | null): boolean {
  if (actionResp === null) return false;
  if (actionResp.contentType === "text") return true;
  if (actionResp.contentType === "json") {
    const json = actionResp.json as Record<string, unknown>;
    if (isPlainObject(json)) {
      if (Object.keys(json).length === 0) return true;
      if (Object.keys(json).length > 1) return true;
      return json["status"] !== "ok";
    }
    return true;
  }
  return true;
}
