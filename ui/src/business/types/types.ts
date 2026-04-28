import type {
  TypeDeclaration,
  TypeDeclarationNormalizeCtx,
  TypeDeclarationValidateCtx,
} from "./TypeDeclaration.ts";
import {
  normalizeBoolean,
  normalizeId,
  normalizeKey,
  normalizeNone,
  normalizeRef,
  normalizeString,
} from "./normalize_helpers.ts";
import type { TagScopeRef, TagSearchFilters } from "@/business/tag";
import {
  validateBoolean,
  validateKey,
  validateRef,
  validateString,
} from "./validate_helpers.ts";
import { valid } from "@seij/common-validation";
import type { ModelDiffScopeCode } from "@/business/model";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const registeredTypes: TypeDeclaration<any>[] = [
  {
    id: "ActorId",
    validate: validateId,
    normalize: normalizeId,
  },
  {
    id: "AttributeKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "AttributeRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "Boolean",
    validate: validateBoolean,
    normalize: normalizeBoolean,
  },
  {
    id: "BusinessKeyKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "BusinessKeyRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "EntityAttributeRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "EntityKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "EntityRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "Fullname",
    validate: validateString,
    normalize: normalizeString,
  },
  {
    id: "Hashtag",
    validate: validateHashtag,
    normalize: normalizeString,
  },
  {
    id: "TextMarkdown",
    validate: validateString,
    normalize: normalizeString,
  },
  {
    id: "TextSingleLine",
    validate: validateString,
    normalize: normalizeString,
  },
  {
    id: "ModelAuthority",
    validate: validateString,
    normalize: normalizeString,
  },
  {
    id: "ModelDiffScope",
    validate: validateNone,
    normalize: normalizeModelDiffScope,
  },
  {
    id: "ModelKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "ModelRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "ModelVersion",
    validate: validateVersion,
    normalize: normalizeVersion,
  },
  {
    id: "PasswordClear",
    validate: validateString,
    normalize: normalizeString,
  },
  {
    id: "PermissionKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "RelationshipAttributeRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "RelationshipCardinality",
    validate: validateString,
    normalize: normalizeString,
  },
  {
    id: "RelationshipKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "RelationshipRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "RelationshipRoleKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "RelationshipRoleRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "RoleId",
    validate: validateId,
    normalize: normalizeId,
  },
  {
    id: "RoleKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "RoleRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "SearchFields",
    validate: validateNone,
    normalize: normalizeNone,
  },
  {
    id: "SearchFilters",
    validate: validateNone,
    normalize: normalizeNone,
  },
  {
    id: "String",
    validate: validateString,
    normalize: normalizeString,
  },
  {
    id: "TagGroupKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "TagGroupRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "TagId",
    validate: validateId,
    normalize: normalizeId,
  },
  {
    id: "TagKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "TagRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "TagSearchFilters",
    validate: validateNone,
    normalize: normalizeNone,
  },
  {
    id: "TagScopeRef",
    validate: validateNone,
    normalize: normalizeNone,
  },
  {
    id: "TypeKey",
    validate: validateKey,
    normalize: normalizeKey,
  },
  {
    id: "TypeRef",
    validate: validateRef,
    normalize: normalizeRef,
  },
  {
    id: "Username",
    validate: validateString,
    normalize: normalizeString,
  },
];

function normalizeModelDiffScope(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): ModelDiffScopeCode | null {
  const str = normalizeString(ctx, value);
  if (str == "structural" || str == "complete") return str;
  return null;
}

function normalizeVersion(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): string | null {
  return normalizeString(ctx, value);
}

function validateId(field: TypeDeclarationValidateCtx, formDatum: unknown) {
  return validateString(field, formDatum);
}

function validateVersion(
  field: TypeDeclarationValidateCtx,
  formDatum: unknown,
) {
  return validateString(field, formDatum);
}

function validateHashtag(
  field: TypeDeclarationValidateCtx,
  formDatum: unknown,
) {
  return validateString(field, formDatum);
}
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function validateNone(_field: TypeDeclarationValidateCtx, _formDatum: unknown) {
  return valid;
}
