import { AuthRole } from "@/business/actor";
import { InputSelect, type InputSelectOption } from "@seij/common-ui";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import {
  adaptPropsRefIdToRawId,
  normalizeValueStringOrEmpty,
} from "./ActionPerformerInput.utils.ts";
import { useRoleRegistry } from "@/components/business/auth-actor";

/**
 * Selects an auth role by id while exchanging ref values with the action performer.
 */
export function ActionPerformerInputRoleRef(props: ActionPerformerInputProps) {
  const wrappedProps = adaptPropsRefIdToRawId(props);
  const valueSafe = normalizeValueStringOrEmpty(wrappedProps.value);
  const roleRegistry = useRoleRegistry();
  const options = createRoleOptions(roleRegistry.findAllRolesSorted());

  return (
    <InputSelect
      value={valueSafe}
      options={options}
      onValueChange={wrappedProps.onValueChange}
      disabled={wrappedProps.disabled}
    />
  );
}

function createRoleOptions(roles: AuthRole[]): InputSelectOption[] {
  return [
    {
      code: "",
      label: "--",
    },
    ...roles.map((role) => ({
      code: role.id,
      label: role.label,
    })),
  ];
}
