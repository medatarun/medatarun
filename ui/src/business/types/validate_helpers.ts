import { invalid, valid } from "@seij/common-validation";
import { type AppMessageKey, appT } from "@/services/appI18n.tsx";
import type { TypeDeclarationValidateCtx } from "@/business/types/TypeDeclaration.ts";

const t = (key: AppMessageKey, values?: Record<string, unknown>) =>
  appT(key, values);

export function validateKey(
  field: TypeDeclarationValidateCtx,
  formDatum: unknown,
) {
  const valid = validateString(field, formDatum);
  if (!valid.valid) return valid;
  if (
    valid.valid &&
    (formDatum === null || formDatum === undefined || formDatum === "")
  )
    return valid;
  if (typeof formDatum !== "string")
    return invalid(t("formValidation_mustBeString"));
  if (formDatum.length > 255) return invalid(t("formValidation_tooLong"));
  if (formDatum.length < 1) return invalid(t("formValidation_tooShort"));
  return valid;
}

export function validateRef(
  field: TypeDeclarationValidateCtx,
  formDatum: unknown,
) {
  const valid = validateString(field, formDatum);
  if (!valid.valid) return valid;
  if (
    valid.valid &&
    (formDatum === null || formDatum === undefined || formDatum === "")
  )
    return valid;
  if (typeof formDatum !== "string")
    return invalid(t("formValidation_mustBeString"));
  if (formDatum.length > 255) return invalid(t("formValidation_tooLong"));
  if (formDatum.length < 1) return invalid(t("formValidation_tooShort"));
  return valid;
}

export function validateRefList(
  field: TypeDeclarationValidateCtx,
  formDatum: unknown,
) {
  if (formDatum === null || formDatum === undefined) {
    if (field.optional) return valid;
    else return invalid(t("formValidation_required"));
  }
  return valid;
}

export function validateBoolean(
  field: TypeDeclarationValidateCtx,
  formDatum: unknown,
) {
  if (formDatum === null || formDatum === undefined) {
    if (field.optional) return valid;
    else return invalid(t("formValidation_required"));
  }
  if (typeof formDatum === "string") {
    if (formDatum === "true" || formDatum === "false") return valid;
    if (formDatum === "") return valid;
    return invalid(t("formValidation_mustBeTrueFalseOrEmpty"));
  }
  if (typeof formDatum !== "boolean")
    return invalid(t("formValidation_mustBeBoolean"));
  else return valid;
}

export function validateString(
  field: TypeDeclarationValidateCtx,
  formDatum: unknown,
) {
  if (formDatum === null || formDatum === undefined) {
    if (field.optional) return valid;
    else return invalid(t("formValidation_required"));
  }
  if (typeof formDatum !== "string")
    return invalid(t("formValidation_mustBeString"));
  if (formDatum.length === 0) {
    if (field.optional) return valid;
    else return invalid(t("formValidation_required"));
  }
  return valid;
}
