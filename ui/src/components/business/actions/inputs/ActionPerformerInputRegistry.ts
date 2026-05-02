import type { FunctionComponent } from "react";
import type { ActionPerformerInputProps } from "./ActionPerformerInputProps.tsx";
import { ActionPerformerInputBoolean } from "./ActionPerformerInputBoolean.tsx";
import { ActionPerformerInputEntityAttributeRef } from "./ActionPerformerInputEntityAttributeRef.tsx";
import { ActionPerformerInputEntityRef } from "./ActionPerformerInputEntityRef.tsx";
import { ActionPerformerInputModelAuthority } from "./ActionPerformerInputModelAuthority.tsx";
import { ActionPerformerInputPasswordClear } from "./ActionPerformerInputPasswordClear.tsx";
import { ActionPerformerInputRelationshipCardinality } from "./ActionPerformerInputRelationshipCardinality.tsx";
import { ActionPerformerInputRoleRef } from "./ActionPerformerInputRoleRef.tsx";
import { ActionPerformerInputSecurityPermission } from "./ActionPerformerInputSecurityPermission.tsx";
import { ActionPerformerInputTextBase } from "./ActionPerformerInputTextBase.tsx";
import { ActionPerformerInputTypeRef } from "./ActionPerformerInputTypeRef.tsx";
import { ActionPerformerInputTextMarkdown } from "./ActionPerformerInputTextMarkdown.tsx";

/**
 * Registry used by the action performer to map a scalar type id to its input component.
 */
export const ACTION_PERFORMER_INPUT_COMPONENTS_BY_TYPE: Record<
  string,
  FunctionComponent<ActionPerformerInputProps>
> = {
  Boolean: ActionPerformerInputBoolean,
  EntityAttributeRef: ActionPerformerInputEntityAttributeRef,
  EntityRef: ActionPerformerInputEntityRef,
  ModelAuthority: ActionPerformerInputModelAuthority,
  PermissionKey: ActionPerformerInputSecurityPermission,
  PasswordClear: ActionPerformerInputPasswordClear,
  RoleRef: ActionPerformerInputRoleRef,
  RelationshipCardinality: ActionPerformerInputRelationshipCardinality,
  TextMarkdown: ActionPerformerInputTextMarkdown,
  TypeRef: ActionPerformerInputTypeRef,
};

export const ACTION_PERFORMER_INPUT_DEFAULT_COMPONENT =
  ActionPerformerInputTextBase;
