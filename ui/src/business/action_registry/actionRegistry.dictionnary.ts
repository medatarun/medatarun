import { actionRegistryStatic } from "./action_registry.static";
import type { DomainTypeMap } from "./action_registry.domain_types.ts";

/**
 * Represents an object with no allowed properties.
 * We use this instead of `{}` so ESLint does not treat primitives as valid values.
 */
type EmptyObject = Record<string, never>;

/**
 * Single action descriptor type inferred from the static registry.
 * This keeps all derived types synchronized with backend-generated metadata.
 */
type RegistryItem = (typeof actionRegistryStatic.items)[number];

type ActionItem = (typeof actionRegistryStatic.items)[number];

/**
 * Union of all action keys declared in the static registry.
 *
 * type ActionKey = "batch_run" | "actor_disable" | "actor_enable" | "actor_get" | "actor_list" | ...
 */
export type ActionKey = ActionItem["actionKey"];

/**
 * Union of all action group keys declared in the static registry.
 *
 * type ActionGroupKey = "batch" | "auth" | "tag" | "model" | "config" | "databases"
 */
export type ActionGroupKey = ActionItem["groupKey"];

/**
 * Picks the action descriptor that matches a given action key.
 * This is the entry point for key-specific payload typing.
 *
 * So for example, type ModelAddTag = ItemByAction<"model_add_tag">, for example,
 * will give the exact type of the DTO
 */
type ItemByAction<K extends ActionKey> = Extract<ActionItem, { actionKey: K }>;

/**
 * Extracts the parameter union for a specific action.
 * Each member of the union represents one parameter from `parameters`.
 *
 * Quite the same as we did for ActionItem at the start for but parameters
 */
type ParamOf<K extends ActionKey> = ItemByAction<K>["parameters"][number];

// Converts registry JSON scalar/container types into TypeScript value types.
// -----------------------------------------------------------------------------

type UnwrapList<T extends string> = T extends `List<${infer Inner}>`
  ? Inner
  : T;
type IsList<T extends string> = T extends `List<${string}>` ? true : false;
type ResolveDomainScalar<T extends string> =
  UnwrapList<T> extends keyof DomainTypeMap
    ? DomainTypeMap[UnwrapList<T>]
    : never;

type ResolveDomainType<T extends string> =
  IsList<T> extends true ? ResolveDomainScalar<T>[] : ResolveDomainScalar<T>;

// Action params type checks
// Fail fast at compile time if registry uses a type not present in DomainTypeMap.
// -----------------------------------------------------------------------------

type RegistryTypeName = UnwrapList<RegistryItem["parameters"][number]["type"]>;
type UnknownRegistryTypes = Exclude<RegistryTypeName, keyof DomainTypeMap>;
type AssertNever<T extends never> = T;

// If there are errors here, it means that all types from Action registry are
// no mapped to the domain types. So we'll get an error it's on purpose.
// Be careful with esline, this is really used for control

// eslint-disable-next-line @typescript-eslint/no-unused-vars
type AllRegistryTypesAreMapped = AssertNever<UnknownRegistryTypes>;

// Turns one registry parameter descriptor into one object field.
// Optional parameters become optional object properties.
// -----------------------------------------------------------------------------

type ParamToField<P> = P extends {
  name: infer N extends string;
  optional: infer O extends boolean;
  type: infer J extends string;
}
  ? O extends true
    ? { [K in N]?: ResolveDomainType<J> }
    : { [K in N]: ResolveDomainType<J> }
  : never;

/**
 * Merges a union of object fragments into a single object type.
 * This is required because parameters are first represented as a union.
 */
type UnionToIntersection<U> = (
  U extends unknown ? (x: U) => void : EmptyObject
) extends (x: infer I) => void
  ? I
  : EmptyObject;

/**
 * Flattens mapped/intersection output for easier IDE display and debugging.
 */
type Simplify<T> = { [K in keyof T]: T[K] };

/**
 * Builds the payload type for one action key from static metadata.
 * Actions without parameters resolve to an explicit empty-object shape.
 */
export type ActionPayload<K extends ActionKey> = [ParamOf<K>] extends [never]
  ? EmptyObject
  : Simplify<UnionToIntersection<ParamToField<ParamOf<K>>>>;

/**
 * Debug function that prints a typed payload for one action key.
 */
export function createAction<K extends ActionKey>(
  action: K,
  payload: ActionPayload<K>,
): { action: K; payload: ActionPayload<K> } {
  return { action, payload };
}
