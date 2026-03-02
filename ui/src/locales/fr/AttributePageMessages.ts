import { type AttributePageMessages } from "../contracts/AttributePageMessages";

export const attributePageMessages: AttributePageMessages = {
  attributePage_modelNotFound:
    "Impossible de trouver le modèle avec l'identifiant [{modelId}].",
  attributePage_parentNotFound:
    "Impossible de trouver le parent de l'attribut [{attributeId}] avec parentType=[{parentType}] et parentId=[{parentId}].",
  attributePage_attributeNotFound:
    "Impossible de trouver l'attribut [{attributeId}] avec parentType=[{parentType}] et parentId=[{parentId}].",
  attributePage_entityEyebrow: "Attribut d'entité",
  attributePage_relationshipEyebrow: "Attribut de relation",
  attributePage_actions: "Actions",
  attributePage_descriptionPlaceholder: "ajouter une description",
  attributePage_keyLabel: "Clé de l'attribut",
  attributePage_fromModelLabel: "Issu du modèle",
  attributePage_tagsLabel: "Tags",
  attributePage_tagsEmpty: "ajouter des tags",
  attributePage_typeLabel: "Type",
  attributePage_identifierBadge: "Identifiant",
  attributePage_requiredLabel: "Obligatoire",
  attributePage_requiredNo: "Non obligatoire",
  attributePage_requiredYes: "Oui, obligatoire",
};
