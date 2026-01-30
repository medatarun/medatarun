import {invalid, valid, type ValidationResult} from "@seij/common-validation";
import type {FormDataType, FormFieldType} from "./action_form.types.ts";

export function validateForm({formData, formFields}: {
  formData: FormDataType,
  formFields: FormFieldType[]
}): Map<string, ValidationResult> {
  const validationResults: Map<string, ValidationResult> = new Map();
  for (const formField of formFields) {
    let result = valid;
    if (formField.type === "String") result = validateString(formField, formData[formField.key])
    else if (formField.type === "List<ActionWithPayload>") result = validateString(formField, formData[formField.key])
    else if (formField.type === "AttributeKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "AttributeRef") result = validateRef(formField, formData[formField.key])
    else if (formField.type === "EntityKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "EntityRef") result = validateRef(formField, formData[formField.key])
    else if (formField.type === "RelationshipKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "RelationshipRef") result = validateRef(formField, formData[formField.key])
    else if (formField.type === "TypeKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "TypeRef") result = validateRef(formField, formData[formField.key])
    else if (formField.type === "ModelKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "ModelRef") result = validateRef(formField, formData[formField.key])
    else if (formField.type === "Hashtag") result = validateHashtag(formField, formData[formField.key])
    else if (formField.type === "ModelVersion") result = validateVersion(formField, formData[formField.key])
    else if (formField.type === "Boolean") result = validateBoolean(formField, formData[formField.key])
    else if (formField.type === "LocalizedText") result = validateString(formField, formData[formField.key])
    else if (formField.type === "LocalizedMarkdown") result = validateString(formField, formData[formField.key])
    else if (formField.type === "RelationshipRoleKey") result = validateKey(formField, formData[formField.key])
    else if (formField.type === "RelationshipCardinality") result = validateString(formField, formData[formField.key])
    else result = invalid("Unsupported type: " + formField.type)
    validationResults.set(formField.key, result)
  }
  return validationResults
}

function validateKey(field: FormFieldType, formDatum: unknown) {
  const valid = validateString(field, formDatum)
  if (!valid.valid) return valid
  if (valid.valid && (formDatum === null || formDatum === undefined || formDatum === "")) return valid
  if (typeof formDatum !== "string") return invalid("Must be a string")
  if (formDatum.length > 255) return invalid("Too long")
  if (formDatum.length < 1) return invalid("Too short")
  return valid
}

function validateRef(field: FormFieldType, formDatum: unknown) {
  const valid = validateString(field, formDatum)
  if (!valid.valid) return valid
  if (valid.valid && (formDatum === null || formDatum === undefined || formDatum === "")) return valid
  if (typeof formDatum !== "string") return invalid("Must be a string")
  if (formDatum.length > 255) return invalid("Too long")
  if (formDatum.length < 1) return invalid("Too short")
  return valid


}

function validateBoolean(field: FormFieldType, formDatum: unknown) {
  if (formDatum === null || formDatum === undefined) {
    if (field.optional) return valid
    else return invalid("Required")
  }
  if (typeof formDatum === "string") {
    if (formDatum === "true" || formDatum === "false") return valid
    if (formDatum === "")return valid
    return invalid("Must be true or false or empty")
  }
  if (typeof formDatum !== "boolean") return invalid("Must be a boolean")
  else return valid
}

function validateString(field: FormFieldType, formDatum: unknown) {
  if (formDatum === null || formDatum === undefined) {
    if (field.optional) return valid
    else return invalid("Required")
  }
  if (typeof formDatum !== "string") return invalid("Must be a string")
  if (formDatum.length === 0) {
    if (field.optional) return valid
    else return invalid("Required")
  }
  return valid
}

function validateVersion(field: FormFieldType, formDatum: unknown) {
  return validateString(field, formDatum)
}

function validateHashtag(field: FormFieldType, formDatum: unknown) {
  return validateString(field, formDatum)
}