import type { ActionPerformerInputComponentsByType } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputRegistry.ts";
import { ActionPerformerInputBoolean } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputBoolean.tsx";
import { ActionPerformerInputEntityAttributeRef } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputEntityAttributeRef.tsx";
import { ActionPerformerInputEntityRef } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputEntityRef.tsx";
import { ActionPerformerInputModelAuthority } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputModelAuthority.tsx";
import { ActionPerformerInputPasswordClear } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputPasswordClear.tsx";
import { ActionPerformerInputRelationshipCardinality } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputRelationshipCardinality.tsx";
import { ActionPerformerInputRoleRef } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputRoleRef.tsx";
import { ActionPerformerInputSecurityPermission } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputSecurityPermission.tsx";
import { ActionPerformerInputTextMarkdown } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputTextMarkdown.tsx";
import { ActionPerformerInputTypeRef } from "@medatarun/ui/components/business/actions/inputs/ActionPerformerInputTypeRef.tsx";

export const ACTION_PERFORMER_INPUT_COMPONENTS_BY_TYPE: ActionPerformerInputComponentsByType =
  {
    Boolean: ActionPerformerInputBoolean,
    EntityAttributeRef: ActionPerformerInputEntityAttributeRef,
    EntityRef: ActionPerformerInputEntityRef,
    ModelAuthority: ActionPerformerInputModelAuthority,
    PasswordClear: ActionPerformerInputPasswordClear,
    PermissionKey: ActionPerformerInputSecurityPermission,
    RelationshipCardinality: ActionPerformerInputRelationshipCardinality,
    RoleRef: ActionPerformerInputRoleRef,
    TextMarkdown: ActionPerformerInputTextMarkdown,
    TypeRef: ActionPerformerInputTypeRef,
  };
