import { type RelationshipDescriptionMessages } from "../contracts/RelationshipDescriptionMessages";

export const relationshipDescriptionMessages: RelationshipDescriptionMessages =
  {
    relationshipDescription_nAry: "Relation à {count} rôles.",
    relationshipDescription_exactlyOneBetween:
      " peut être associé à exactement un ",
    relationshipDescription_atMostOneBetween:
      " peut être associé à au plus un ",
    relationshipDescription_oneOrMoreBetween:
      " peut être associé à un ou plusieurs ",
    relationshipDescription_withoutMaximumPrefix:
      " peut être associé à ",
    relationshipDescription_withoutMaximumSuffix:
      ", sans maximum défini.",
    relationshipDescription_genericBetween: " peut être associé à ",
  };
