import { useEffect, useState } from "react";
import { InputCombobox } from "@seij/common-ui";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { normalizeValueStringOrEmpty } from "./ActionPerformerInput.utils.ts";

export function ActionPerformerInputModelAuthority({
  value,
  disabled,
  onValueChange,
}: ActionPerformerInputProps) {
  // TODO InputCombobox does not expose an input ref yet, so we cannot wire this forwarded ref for now.

  const valueSafe = normalizeValueStringOrEmpty(value);

  const options = [
    { code: "system", label: "System" },
    { code: "canonical", label: "Canonical" },
  ];

  const [searchQuery, setSearchQuery] = useState<string>(() => {
    const selected = options.find((it) => it.code === valueSafe);
    return selected?.label ?? valueSafe;
  });

  useEffect(() => {
    const selected = options.find((it) => it.code === value);
    setSearchQuery(selected?.label ?? "");
  }, [valueSafe]);

  return (
    <div>
      <InputCombobox
        options={options}
        searchQuery={searchQuery}
        placeholder="Select authority"
        disabled={disabled}
        onValueChangeQuery={(query) => setSearchQuery(query)}
        onValueChange={(nextValue) => {
          onValueChange(nextValue);
          const selected = options.find((it) => it.code === nextValue);
          setSearchQuery(selected?.label ?? nextValue);
        }}
      />
    </div>
  );
}
