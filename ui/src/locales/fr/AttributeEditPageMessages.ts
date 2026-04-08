import { type AttributeEditPageMessages } from "../contracts/AttributeEditPageMessages";

export const attributeEditPageMessages: AttributeEditPageMessages = {
  attributeEditPage_modelNotFound:
    "Impossible de trouver le modèle avec l'identifiant [{modelId}].",
  attributeEditPage_parentNotFound:
    "Impossible de trouver le parent de l'attribut [{attributeId}] avec parentType=[{parentType}] et parentId=[{parentId}].",
  attributeEditPage_attributeNotFound:
    "Impossible de trouver l'attribut [{attributeId}] avec parentType=[{parentType}] et parentId=[{parentId}].",
  attributeEditPage_entityEyebrow: "Attribut d'entité",
  attributeEditPage_relationshipEyebrow: "Attribut de relation",
  attributeEditPage_actions: "Actions",
  attributeEditPage_descriptionPlaceholder: "ajouter une description",
  attributeEditPage_keyLabel: "Clé de l'attribut",
  attributeEditPage_tagsLabel: "Tags",
  attributeEditPage_tagsEmpty: "ajouter des tags",
  attributeEditPage_typeLabel: "Type",
  attributeEditPage_identifierBadge: "Identifiant",
  attributeEditPage_identifierLabel: "Identifiant",
  attributeEditPage_requiredLabel: "Obligatoire",
  attributeEditPage_requiredNo: "Non obligatoire",
  attributeEditPage_requiredYes: "Oui, obligatoire",
};
