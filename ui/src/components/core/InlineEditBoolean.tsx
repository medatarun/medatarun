import { type ReactNode, useState } from "react";
import { InlineEditSingleLineController } from "./InlineEditSingleLineController.tsx";
import { SwitchButton } from "@seij/common-ui";

export function InlineEditBoolean({
  value,
  disabled = false,
  children,
  labelTrue,
  labelFalse,
  onChange,
}: {
  value: boolean;
  disabled?: boolean;
  children: ReactNode;
  labelTrue: string;
  labelFalse: string;
  onChange: (value: boolean) => Promise<unknown>;
}) {
  const [editValue, setEditValue] = useState(value);

  const handleEditStart = async () => {
    setEditValue(value);
  };

  const handleEditOk = async () => {
    await onChange(editValue);
  };

  const handleEditCancel = async () => {
    setEditValue(value);
  };

  return (
    <InlineEditSingleLineController
      disabled={disabled}
      editor={({ pending }) => (
        <div>
          <SwitchButton
            value={editValue}
            disabled={pending}
            onValueChange={setEditValue}
            labelTrue={labelTrue}
            labelFalse={labelFalse}
          />
        </div>
      )}
      onEditStart={handleEditStart}
      onEditOK={handleEditOk}
      onEditCancel={handleEditCancel}
    >
      {children}
    </InlineEditSingleLineController>
  );
}
