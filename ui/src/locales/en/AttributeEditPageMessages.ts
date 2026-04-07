import { type AttributeEditPageMessages } from "../contracts/AttributeEditPageMessages";

export const attributeEditPageMessages: AttributeEditPageMessages = {
  attributeEditPage_modelNotFound: "Can not find model with id [{modelId}].",
  attributeEditPage_parentNotFound:
    "Can not find parent of attribute [{attributeId}] with parentType=[{parentType}] and parentId=[{parentId}].",
  attributeEditPage_attributeNotFound:
    "Can not find attribute [{attributeId}] with parentType=[{parentType}] and parentId=[{parentId}].",
  attributeEditPage_entityEyebrow: "Attribute of entity",
  attributeEditPage_relationshipEyebrow: "Attribute of relationship",
  attributeEditPage_actions: "Actions",
  attributeEditPage_descriptionPlaceholder: "add description",
  attributeEditPage_keyLabel: "Attribute key",
  attributeEditPage_tagsLabel: "Tags",
  attributeEditPage_tagsEmpty: "add tags",
  attributeEditPage_typeLabel: "Type",
  attributeEditPage_identifierBadge: "Identifier",
  attributeEditPage_identifierLabel: "Identifier",
  attributeEditPage_requiredLabel: "Required",
  attributeEditPage_requiredNo: "Not required",
  attributeEditPage_requiredYes: "Yes, required",
};
