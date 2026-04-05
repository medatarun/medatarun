import { usePermissionRegistry } from "@/business/config";
import { InputSelect, type InputSelectOption } from "@seij/common-ui";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import {
  adaptPropsValueNullableToValueEmpty,
  normalizeValueStringOrEmpty,
} from "./ActionPerformerInput.utils.ts";

export function ActionPerformerInputSecurityPermission(
  props: ActionPerformerInputProps,
) {
  const wrappedProps = adaptPropsValueNullableToValueEmpty(props);
  const valueSafe = normalizeValueStringOrEmpty(wrappedProps.value);
  const { registry } = usePermissionRegistry();
  const options = createPermissionOptions(registry.findAll());

  return (
    <InputSelect
      value={valueSafe}
      options={options}
      disabled={wrappedProps.disabled}
      onValueChange={wrappedProps.onValueChange}
    />
  );
}

function createPermissionOptions(
  permissions: { id: string; name: string | null }[],
): InputSelectOption[] {
  return [
    {
      code: "",
      label: "--",
    },
    ...permissions.map((permission) => ({
      code: permission.id,
      label: permission.name ? `${permission.name} [${permission.id}]` : `[${permission.id}]`,
    })),
  ];
}
