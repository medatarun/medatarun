import { type FormValidationMessages } from "../contracts/FormValidationMessages";

export const formValidationMessages: FormValidationMessages = {
  formValidation_unsupportedType: "Type non pris en charge : {type}",
  formValidation_mustBeString: "La valeur doit être une chaîne de caractères",
  formValidation_tooLong: "La valeur est trop longue",
  formValidation_tooShort: "La valeur est trop courte",
  formValidation_required: "Champ obligatoire",
  formValidation_mustBeTrueFalseOrEmpty:
    "La valeur doit être true, false ou vide",
  formValidation_mustBeBoolean: "La valeur doit être un booléen",
};
