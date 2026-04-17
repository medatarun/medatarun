import * as React from "react";
import {
  forwardRef,
  type PropsWithChildren,
  useEffect,
  useRef,
  useState,
} from "react";

import {
  Avatar,
  Tag,
  TagPicker,
  TagPickerControl,
  TagPickerGroup,
  TagPickerInput,
  TagPickerList,
  TagPickerOption,
  type TagPickerProps,
} from "@fluentui/react-components";
import { InlineEditSingleLineLayout } from "./InlineEditSingleLineLayout.tsx";
import { Tags, type TagScopeRef, useTags } from "@/business/tag";
import { useActionPerformer } from "@/components/business/actions/ActionPerformerHook.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/components/business/actions";

const CREATE_OPTION_PREFIX = "__create__:";

export function InlineEditTags({
  value,
  scope,
  children,
  onChange,
  displayedSubject,
}: {
  value: string[];
  scope: TagScopeRef;
  onChange: (value: string[]) => Promise<unknown>;
  displayedSubject: ActionDisplayedSubject;
} & PropsWithChildren) {
  const [values, setValues] = useState(value);
  const ref = useRef<HTMLInputElement>(null);
  const { tags } = useTags(scope);
  const { performAction, state } = useActionPerformer();
  const [requestedCreatedTagKey, setRequestedCreatedTagKey] = useState<
    string | null
  >(null);
  const [waitingCreatedTagResolution, setWaitingCreatedTagResolution] =
    useState(false);
  const previousStateKindRef = useRef(state.kind);

  useEffect(() => {
    const previousStateKind = previousStateKindRef.current;
    previousStateKindRef.current = state.kind;

    if (requestedCreatedTagKey == null) {
      return;
    }
    const actionFinished =
      previousStateKind === "running" &&
      state.kind === "done" &&
      state.request.actionGroupKey === "tag" &&
      state.request.actionKey === "tag_local_create";

    if (actionFinished) {
      setWaitingCreatedTagResolution(true);
    }

    if (!waitingCreatedTagResolution && !actionFinished) {
      return;
    }

    const createdTag = tags.findTagByScopeAndKey(scope, requestedCreatedTagKey);
    if (!createdTag) {
      return;
    }
    setValues((previousValues) =>
      previousValues.includes(createdTag.id)
        ? previousValues
        : [...previousValues, createdTag.id],
    );
    setRequestedCreatedTagKey(null);
    setWaitingCreatedTagResolution(false);
  }, [requestedCreatedTagKey, scope, state, tags, waitingCreatedTagResolution]);

  const actionCtxTag = (key: string) =>
    new ActionCtxMapping(
      [
        {
          actionParamKey: "scopeRef",
          defaultValue: () => scope,
          readonly: true,
          visible: false,
        },
        {
          actionParamKey: "key",
          defaultValue: () => key,
        },
      ],
      displayedSubject,
    );

  const handleEditStart = async () => {
    setValues(value);
  };

  const handleEditStarted = () => {
    ref?.current?.focus();
  };

  const handeEditOk = async () => {
    await onChange(values);
  };
  const handleEditCancel = async () => {
    setValues(value);
    setRequestedCreatedTagKey(null);
    setWaitingCreatedTagResolution(false);
  };

  const handleCreateTag = (key: string) => {
    setRequestedCreatedTagKey(key);
    setWaitingCreatedTagResolution(false);
    performAction({
      actionGroupKey: "tag",
      actionKey: "tag_local_create",
      ctx: actionCtxTag(key),
    });
  };

  return (
    <InlineEditSingleLineLayout
      editor={({ commit, cancel, pending }) => (
        <InputWithKeys
          ref={ref}
          disabled={pending}
          scope={scope}
          tags={tags}
          values={values}
          onCreateTag={handleCreateTag}
          onChange={setValues}
          onCommit={commit}
          onCancel={cancel}
        />
      )}
      onEditStart={handleEditStart}
      onEditStarted={handleEditStarted}
      onEditOK={handeEditOk}
      onEditCancel={handleEditCancel}
    >
      {children}
    </InlineEditSingleLineLayout>
  );
}

type InputWithKeysProps = {
  values: string[];
  disabled: boolean;
  scope: TagScopeRef;
  tags: Tags;
  onCommit: () => void;
  onCancel: () => void;
  onCreateTag: (key: string) => void;
  onChange: (value: string[]) => void;
};
const InputWithKeys = forwardRef<HTMLInputElement, InputWithKeysProps>(
  (
    {
      values,
      disabled,
      scope,
      tags,
      onChange,
      onCommit,
      onCancel,
      onCreateTag,
    },
    ref,
  ) => {
    const { t } = useAppI18n();
    // Input value is what the user currently types in the input field
    const [inputValue, setInputValue] = useState("");
    // Some keyboards used to compose characters in languages as Japaneses send and event
    // that tells that the user is composing a character. We must take that in account
    // when managing the Enter key
    const [isComposing, setIsComposing] = useState(false);
    // Tels when the option list is opened or not
    const [isOptionSelectOpen, setIsOptionSelectOpen] = useState(false);

    // Options to display in the select box (list of existing tags)
    const options = tags.search(inputValue, values);

    const trimmedInputValue = inputValue.trim();

    // The "Create ..." message and its corresponding option appear when the user
    // types characters that match no tags. We propose him a "fake" option in the list
    // that will open the tag creation form in a Model
    const canCreateTag =
      scope.type !== "global" &&
      trimmedInputValue !== "" &&
      tags.findTagByScopeAndKey(scope, trimmedInputValue) == null;

    // Indicates if there are still tags we can select
    const hasAvailableOptions = canCreateTag || options.length > 0;

    const onOptionSelect: TagPickerProps["onOptionSelect"] = (_, data) => {
      const selectedOption = data.value;
      if (
        typeof selectedOption === "string" &&
        selectedOption.startsWith(CREATE_OPTION_PREFIX)
      ) {
        onCreateTag(selectedOption.slice(CREATE_OPTION_PREFIX.length));
        setInputValue("");
        setIsOptionSelectOpen(false);
        return;
      }
      onChange(data.selectedOptions);
      setInputValue("");
      setIsOptionSelectOpen(false);
    };
    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
      setInputValue(event.currentTarget.value);
      setIsOptionSelectOpen(true);
    };

    // Some keyboards used to compose characters in languages such as Japanese or Chinese
    // emit Enter before the text is fully validated. We ignore that Enter so the picker
    // does not commit the edit or select an option too early.
    const handleKeyDown = (e: React.KeyboardEvent) => {
      if (e.key === "Enter") {
        if (isComposing) return;
        if (isOptionSelectOpen) return;
        if (inputValue.trim() === "") {
          e.preventDefault();
          onCommit();
        }
      }
      if (e.key === "Escape") {
        e.preventDefault();
        onCancel();
      }
    };

    return (
      <div style={{ position: "relative" }}>
        <TagPicker
          open={isOptionSelectOpen}
          onOpenChange={(_, data) => setIsOptionSelectOpen(data.open)}
          onOptionSelect={onOptionSelect}
          selectedOptions={values}
        >
          <TagPickerControl>
            <TagPickerGroup aria-label={t("inlineEditTags_selectedAriaLabel")}>
              {values.map((option, index) => (
                <Tag
                  key={index}
                  shape="rounded"
                  media={
                    <Avatar
                      aria-hidden
                      name={tags.formatLabel(option)}
                      color="colorful"
                    />
                  }
                  value={option}
                >
                  {tags.formatLabel(option)}
                </Tag>
              ))}
            </TagPickerGroup>
            <TagPickerInput
              ref={ref}
              value={inputValue}
              //style={{width: "100%"}}
              aria-label={t("inlineEditTags_inputAriaLabel")}
              disabled={disabled}
              onFocus={() => setIsOptionSelectOpen(true)}
              onCompositionStart={() => setIsComposing(true)}
              onCompositionEnd={() => setIsComposing(false)}
              onKeyDown={handleKeyDown}
              onChange={handleChange}
            />
          </TagPickerControl>
          <TagPickerList>
            {!hasAvailableOptions && (
              <div
                style={{
                  padding: "8px 12px",
                  color: "var(--colorNeutralForeground3)",
                }}
              >
                {t("inlineEditTags_empty")}
              </div>
            )}
            {canCreateTag && (
              <TagPickerOption
                key={CREATE_OPTION_PREFIX + trimmedInputValue}
                value={CREATE_OPTION_PREFIX + trimmedInputValue}
                text={t("inlineEditTags_createOption", {
                  value: trimmedInputValue,
                })}
              >
                {t("inlineEditTags_createOption", { value: trimmedInputValue })}
              </TagPickerOption>
            )}
            {options.map((option) => (
              <TagPickerOption
                key={option.id}
                value={option.id}
                text={tags.formatLabel(option.id)}
              >
                {tags.formatLabel(option.id)}
              </TagPickerOption>
            ))}
          </TagPickerList>
        </TagPicker>
      </div>
    );
  },
);
