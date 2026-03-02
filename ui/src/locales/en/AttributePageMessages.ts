import { type AttributePageMessages } from "../contracts/AttributePageMessages";

export const attributePageMessages: AttributePageMessages = {
  attributePage_modelNotFound: "Can not find model with id [{modelId}].",
  attributePage_parentNotFound:
    "Can not find parent of attribute [{attributeId}] with parentType=[{parentType}] and parentId=[{parentId}].",
  attributePage_attributeNotFound:
    "Can not find attribute [{attributeId}] with parentType=[{parentType}] and parentId=[{parentId}].",
  attributePage_entityEyebrow: "Attribute of entity",
  attributePage_relationshipEyebrow: "Attribute of relationship",
  attributePage_actions: "Actions",
  attributePage_descriptionPlaceholder: "add description",
  attributePage_keyLabel: "Attribute key",
  attributePage_fromModelLabel: "From model",
  attributePage_tagsLabel: "Tags",
  attributePage_tagsEmpty: "add tags",
  attributePage_typeLabel: "Type",
  attributePage_identifierBadge: "Identifier",
  attributePage_requiredLabel: "Required",
  attributePage_requiredNo: "Not required",
  attributePage_requiredYes: "Yes, required",
};
