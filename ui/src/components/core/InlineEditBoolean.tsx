import { type ReactNode, useState } from "react";
import { InlineEditSingleLineLayout } from "@/components/core/InlineEditSingleLineLayout.tsx";
import { SwitchButton } from "@seij/common-ui";

export function InlineEditBoolean({
  value,
  children,
  labelTrue,
  labelFalse,
  onChange,
}: {
  value: boolean;
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
    <InlineEditSingleLineLayout
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
    </InlineEditSingleLineLayout>
  );
}
