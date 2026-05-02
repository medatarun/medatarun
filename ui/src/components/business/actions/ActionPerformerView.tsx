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
import {
  type ActionCtx,
  type ActionPerformerRequestState,
  type ActionRequest,
  type ActionResp,
} from "@/business/action-performer";
import {
  type ActionFormData,
  type ActionFormFieldDescription,
  ActionFormService,
} from "@/business/action-form";
import { ActionDescriptor } from "@/business/action_registry";
import ReactMarkdown from "react-markdown";
import {
  combineValidationResults,
  type ValidationResult,
} from "@seij/common-validation";
import { Button, ErrorBox } from "@seij/common-ui";
import { isPlainObject } from "lodash-es";
import { toProblem } from "@seij/common-types";
import { useAppI18n } from "@/services/appI18n.tsx";
import type {
  ActionPerformerInputElement,
  ActionPerformerInputProps,
} from "./inputs/ActionPerformerInputProps.tsx";
import { ActionPerformerInputList } from "./inputs/ActionPerformerInputList.tsx";
import { TypeRegistryInstance } from "@/business/types/TypeRegistry.ts";
import {
  ACTION_PERFORMER_INPUT_COMPONENTS_BY_TYPE,
  ACTION_PERFORMER_INPUT_DEFAULT_COMPONENT,
} from "./inputs/ActionPerformerInputRegistry.ts";
import { useActionPerformer } from "@/components/business/actions/action-performer-hook.tsx";
import { useActionRegistry } from "@/components/business/actions/action_registry.hooks.ts";
import { Markdown } from "@/components/core/Markdown.tsx";
import { Logger } from "tslog";

const DEBUG = false;
const logger = new Logger();
export function ActionPerformerView() {
  // Separate state extraction here, so that when state changes all ActionPerformView is redrawn
  const { performer } = useActionPerformer();
  const actionRegistry = useActionRegistry();

  const state = performer.getLastStartedRequestState();
  if (state == null) {
    return null;
  }
  const request = state.request;
  const action = actionRegistry.findActionOptional(
    request.actionGroupKey,
    request.actionKey,
  );
  if (!action) {
    return null;
  }

  const defaultFormData: ActionFormData = {};
  for (const actionParam of action.parameters) {
    // Take the default value from parameters. Normalize the value so that
    // we always have null (not undefined)
    defaultFormData[actionParam.name] =
      request.ctx.getDefaultValue(actionParam.name, request) ?? null;
  }

  const formFields = createFormFields(action, request.ctx);

  return (
    <ActionPerformerViewLoaded
      key={state.requestId}
      state={state}
      request={request}
      action={action}
      defaultFormData={defaultFormData}
      formFields={formFields}
    />
  );
}

export function ActionPerformerViewLoaded({
  state,
  request,
  action,
  defaultFormData,
  formFields,
}: {
  state: ActionPerformerRequestState;
  request: ActionRequest;
  action: ActionDescriptor;
  defaultFormData: ActionFormData;
  formFields: ActionFormFieldDescription[];
}) {
  const { t } = useAppI18n();
  const actionRegistry = useActionRegistry();
  const { confirmAction, cancelAction, finishAction, performer } =
    useActionPerformer();
  const [actionResp, setActionResp] = useState<ActionResp | null>(null);
  const [formData, setFormData] = useState<ActionFormData>(defaultFormData);
  const firstInputRef = useRef<ActionPerformerInputElement>(null);

  const formService = new ActionFormService(
    TypeRegistryInstance,
    actionRegistry,
  );

  const displayExecute = state.kind == "pendingUser" || state.kind == "error";
  const displayCancel =
    state.kind == "pendingUser" ||
    state.kind == "running" ||
    state.kind == "error";
  const displayFinish = state.kind == "done";

  const validationResults = formService.validateForm(formData, formFields);
  const validationResult = combineValidationResults([
    ...validationResults.values(),
  ]);
  const valid = validationResult.valid;

  const onValidate = async () => {
    const payload = formService.formDataNormalize(
      action.actionGroupKey,
      action.key,
      formData,
    );
    const output = await confirmAction(state.requestId, payload);
    setActionResp(output);
  };

  const onCancel = () => {
    cancelAction(state.requestId);
  };

  const onFinish = () => {
    finishAction(state.requestId);
  };

  const handleChangeFormFieldInput = (
    field: ActionFormFieldDescription,
    value: unknown,
  ) => {
    const next = { ...formData, [field.key]: value };
    logger.debug(
      "ActionPerformerView:handleChangeFormFieldInput",
      formData,
      next,
    );
    setFormData(next);
  };

  useEffect(() => {
    const isDone = state.kind === "done";
    const display = hasResultToDisplay(actionResp);
    if (isDone && !display) {
      finishAction(state.requestId);
    }
  }, [state.kind, actionResp, finishAction, state.requestId]);

  useEffect(() => {
    if (state.kind !== "done") return;

    performer.resolveNavigationAfterSuccess({
      request: state.request,
    });
  }, [action, performer, state]);

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
              {action.description && <Markdown value={action.description} />}
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
  request: ActionRequest;
  field: ActionFormFieldDescription;
  value: unknown;
  validationResult: ValidationResult | undefined;
  onChange: (field: ActionFormFieldDescription, value: unknown) => void;
  inputRef?: Ref<ActionPerformerInputElement>;
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

function createFormFields(action: ActionDescriptor, actionCtx: ActionCtx) {
  const formFields: ActionFormFieldDescription[] = [];
  action.parameters.forEach((param) => {
    const isReadOnly = actionCtx.isReadonly(param.name) ?? false;
    const isVisible = actionCtx.isVisible(param.name) ?? true;
    const isPresent = actionCtx.isPresent(param.name);
    const field: ActionFormFieldDescription = {
      key: param.name,
      title: param.title ?? param.name,
      description: param.description,
      optional: param.optional,
      type: param.type,
      order: param.order,
      // If the parameter has not been specified, the field is always editable.
      // If the parameter has been specified, take the value of readonly
      readonly: !isPresent ? false : isReadOnly,
      // If the parameter has not been specified, always show the field.
      // If the parameter has been specified, take the value of readonly
      visible: !isPresent ? true : isVisible,
    };
    formFields.push(field);
  });
  return sortFields(formFields);
}

function sortFields(
  fields: ActionFormFieldDescription[],
): ActionFormFieldDescription[] {
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
