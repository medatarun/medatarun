import type { ActionRegistryDto } from "./action-registry-dto.ts";

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface ActionRegistryRegister {}

type EmptyActionRegistry = {
  readonly items: readonly [];
};

export type RegisteredActionRegistry = ActionRegistryRegister extends {
  registry: infer Registry extends ActionRegistryDto;
}
  ? Registry
  : EmptyActionRegistry;
