import type {
  ActionDisplayedSubject,
  ActionPerformerRequestParams,
} from "@/components/business/actions/ActionPerformer.tsx";
import { refid } from "@/business/action_runner";

export const createActionTemplateGeneral = () => ({});
export const createActionTemplateEntity = (
  modelId: string,
  entityId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    entityRef: refid(entityId),
  };
};
export const createActionTemplateEntityAttribute = (
  modelId: string,
  entityId: string,
  attributeId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    entityRef: refid(entityId),
    attributeRef: refid(attributeId),
  };
};
export const createActionTemplateRelationshipAttribute = (
  modelId: string,
  relationshipId: string,
  attributeId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    relationshipRef: refid(relationshipId),
    attributeRef: refid(attributeId),
  };
};
export const createActionTemplateModel = (
  modelId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
  };
};
export const createActionTemplateType = (
  modelId: string,
  typeId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    typeRef: refid(typeId),
  };
};
export const createActionTemplateRelationship = (
  modelId: string,
  relationshipId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    relationshipRef: refid(relationshipId),
  };
};
export const createActionTemplateEntityForRelationships = (
  modelId: string,
  entityId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    roleAEntityRef: refid(entityId),
  };
};
export const createActionTemplateRelationshipRole = (
  modelId: string,
  relationshipId: string,
  roleId: string,
): ActionPerformerRequestParams => {
  return {
    modelRef: refid(modelId),
    relationshipRef: refid(relationshipId),
    relationshipRoleRef: refid(roleId),
  };
};

export const createDisplayedSubjectModel = (
  modelId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "model",
  refs: { modelId: modelId },
});

export const createDisplayedSubjectEntity = (
  modelId: string,
  entityId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "entity",
  refs: { modelId: modelId, entityId: entityId },
});

export const createDisplayedSubjectEntityAttribute = (
  modelId: string,
  entityId: string,
  attributeId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "entity_attribute",
  refs: {
    modelId: modelId,
    entityId: entityId,
    attributeId: attributeId,
  },
});

export const createDisplayedSubjectType = (
  modelId: string,
  typeId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "type",
  refs: { modelId: modelId, typeId: typeId },
});

export const createDisplayedSubjectRelationship = (
  modelId: string,
  relationshipId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "relationship",
  refs: {
    modelId: modelId,
    relationshipId: relationshipId,
  },
});

export const createDisplayedSubjectRelationshipAttribute = (
  modelId: string,
  relationshipId: string,
  attributeId: string,
): ActionDisplayedSubject => ({
  kind: "resource",
  type: "relationship_attribute",
  refs: {
    modelId: modelId,
    relationshipId: relationshipId,
    attributeId: attributeId,
  },
});
