import type { Model } from "@/business/model/model.domain.ts";
import {
  ActionCtxMapping,
  type ActionDisplayedSubject,
} from "@/components/business/actions";
import type {
  AttributeDto,
  BusinessKeyDto,
  EntityDto,
  RelationshipDto,
  RelationshipRoleDto,
  TypeDto,
} from "@/business/model/model.dto.ts";
import { stubFalse } from "lodash-es";

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

export const createActionCtxModel = (
  model: Model,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionKey: "model_update_authority",
        actionParamKey: "value",
        defaultValue: () => model.authority,
      },
      {
        actionGroupKey: "tag",
        actionKey: /^tag_local/,
        actionParamKey: "scopeRef",
        defaultValue: () => ({ type: "model", id: model.id }),
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
};

export const createActionCtxEntity = (
  model: Model,
  entity: EntityDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "entityRef",
        defaultValue: () => "id:" + entity.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "roleAEntityRef",
        defaultValue: () => "id:" + entity.id,
        readonly: true,
        visible: false,
      },
      {
        actionKey: "entity_primary_key_update",
        actionParamKey: "attributeRef",
        defaultValue: () => model.findEntityPKAttributes(entity.id),
        readonly: false,
        visible: true,
      },
    ],
    displayedSubject,
  );
};

export const createActionCtxEntityAttribute = (
  model: Model,
  entity: EntityDto,
  attribute: AttributeDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "entityRef",
        defaultValue: () => "id:" + entity.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "attributeRef",
        defaultValue: () => "id:" + attribute.id,
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
};
export const createActionCtxBusinessKey = (
  model: Model,
  entity: EntityDto,
  businessKey: BusinessKeyDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "entityRef",
        defaultValue: () => "id:" + entity.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "businessKeyRef",
        defaultValue: () => "id:" + businessKey.id,
        readonly: true,
        visible: false,
      },
      {
        actionKey: "business_key_update_key",
        actionParamKey: "value",
        defaultValue: () => businessKey.key,
      },
      {
        actionKey: "business_key_update_name",
        actionParamKey: "value",
        defaultValue: () => businessKey.name,
      },
      {
        actionKey: "business_key_update_description",
        actionParamKey: "value",
        defaultValue: () => businessKey.description,
      },
      {
        actionKey: "business_key_update_participants",
        actionParamKey: "value",
        defaultValue: () =>
          businessKey.participants.map((it) => "id:" + it) ?? [],
      },
    ],
    displayedSubject,
  );
};

export const createActionCtxRelationship = (
  model: Model,
  relationship: RelationshipDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "relationshipRef",
        defaultValue: () => "id:" + relationship.id,
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
};

export const createActionCtxRelationshipAttribute = (
  model: Model,
  relationship: RelationshipDto,
  attribute: AttributeDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "relationshipRef",
        defaultValue: () => "id:" + relationship.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "attributeRef",
        defaultValue: () => "id:" + attribute.id,
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
};

export const createActionCtxRelationshipRole = (
  model: Model,
  relationship: RelationshipDto,
  role: RelationshipRoleDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "relationshipRef",
        defaultValue: () => "id:" + relationship.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "relationshipRoleRef",
        defaultValue: () => "id:" + role.id,
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
};
export const createActionCtxType = (
  model: Model,
  type: TypeDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "typeRef",
        defaultValue: () => "id:" + type.id,
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
};
