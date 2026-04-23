import { InputCombobox } from "@seij/common-ui";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import {
  adaptPropsValueNullableToValueEmpty,
  normalizeValueStringOrEmpty,
} from "./ActionPerformerInput.utils.ts";
import { useEffect, useState } from "react";
import { usePermissionRegistry } from "@/components/business/config";

export function ActionPerformerInputSecurityPermission(
  props: ActionPerformerInputProps,
) {
  const wrappedProps = adaptPropsValueNullableToValueEmpty(props);
  const valueSafe = normalizeValueStringOrEmpty(wrappedProps.value);
  const { registry } = usePermissionRegistry();
  const options = createPermissionOptions(registry.findAll());
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (valueSafe === "") {
      setSearchQuery("");
      return;
    }
    const selected = options.find((option) => option.code === valueSafe);
    setSearchQuery(selected?.label ?? valueSafe);
  }, [options, valueSafe]);

  return (
    <InputCombobox
      searchQuery={searchQuery}
      placeholder="Select a permission"
      options={options}
      noOptionsMessage="No permissions"
      disabled={wrappedProps.disabled}
      onValueChangeQuery={setSearchQuery}
      onValueChange={(newValue) => {
        wrappedProps.onValueChange(newValue);
        const selected = options.find((option) => option.code === newValue);
        setSearchQuery(selected?.label ?? newValue);
      }}
    />
  );
}

function createPermissionOptions(
  permissions: { id: string; name: string | null }[],
): { code: string; label: string }[] {
  return permissions.map((permission) => ({
    code: permission.id,
    label: permission.name ?? permission.id,
  }));
}
