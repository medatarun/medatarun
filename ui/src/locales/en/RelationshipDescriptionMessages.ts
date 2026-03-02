import { type RelationshipDescriptionMessages } from "../contracts/RelationshipDescriptionMessages";

export const relationshipDescriptionMessages: RelationshipDescriptionMessages =
  {
    relationshipDescription_nAry: "{count}-ary relationship.",
    relationshipDescription_exactlyOneBetween:
      " can be associated with exactly one ",
    relationshipDescription_atMostOneBetween:
      " can be associated with at most one ",
    relationshipDescription_oneOrMoreBetween:
      " can be associated with one or more ",
    relationshipDescription_withoutMaximumPrefix:
      " can be associated with ",
    relationshipDescription_withoutMaximumSuffix:
      ", with no defined maximum.",
    relationshipDescription_genericBetween: " can be associated with ",
  };
