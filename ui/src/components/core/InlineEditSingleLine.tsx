import * as React from "react";
import { forwardRef, type PropsWithChildren, useRef, useState } from "react";
import { Logger } from "tslog";
import { Input } from "@fluentui/react-components";
import { InlineEditSingleLineController } from "./InlineEditSingleLineController.tsx";

const logger = new Logger();

export function InlineEditSingleLine({
  value,
  children,
  onChange,
  disabled = false,
}: {
  value: string | null | undefined;
  onChange: (value: string) => Promise<unknown>;
  disabled?: boolean;
} & PropsWithChildren) {
  const [editValue, setEditValue] = useState(value ?? "");
  const ref = useRef<HTMLInputElement>(null);

  const handleEditStart = async () => {
    logger.debug("handleEditStart");
    setEditValue(value ?? "");
  };

  const handleEditStarted = () => {
    logger.debug("handleEditStarted");
    ref?.current?.focus();
  };

  const handeEditOk = async () => {
    await onChange(editValue);
  };
  const handleEditCancel = async () => {
    setEditValue("");
  };

  if (disabled) return children;

  return (
    <InlineEditSingleLineController
      editor={({ commit, cancel, pending }) => (
        <InputWithKeys
          ref={ref}
          disabled={pending}
          editValue={editValue}
          onChange={setEditValue}
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
    </InlineEditSingleLineController>
  );
}

type InputWithKeysProps = {
  editValue: string;
  disabled: boolean;
  onCommit: () => void;
  onCancel: () => void;
  onChange: (value: string) => void;
};
const InputWithKeys = forwardRef<HTMLInputElement, InputWithKeysProps>(
  ({ editValue, disabled, onChange, onCommit, onCancel }, ref) => {
    const [isComposing, setIsComposing] = useState(false);
    return (
      <Input
        ref={ref}
        value={editValue}
        style={{ width: "100%" }}
        disabled={disabled}
        onCompositionStart={() => setIsComposing(true)}
        onCompositionEnd={() => setIsComposing(false)}
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            if (isComposing) return; // Asian inputs
            e.preventDefault();
            onCommit();
          }
          if (e.key === "Escape") {
            e.preventDefault();
            onCancel();
          }
        }}
        onChange={(_, data) => onChange(data.value)}
      />
    );
  },
);
