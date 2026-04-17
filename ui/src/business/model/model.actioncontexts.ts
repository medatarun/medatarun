import type { Model } from "@/business/model/model.domain.ts";
import {
  type ActionCtx,
  ActionCtxMapping,
  type ActionDisplayedSubject,
  type ActionPerformerRequest,
} from "@/components/business/actions";
import type { RelationshipDto } from "@/business/model/model.dto.ts";
import { Tag } from "@/business/tag";

export const createActionCtxModel = (
  model: Model,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping<Model>(
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

export const createActionCtxRelationship = (
  model: Model,
  relationship: RelationshipDto,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping<Model>(
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

export const createActionCtxTag = (
  model: Model,
  tag: Tag,
  displayedSubject: ActionDisplayedSubject,
) => {
  return new ActionCtxMapping<Model>(
    [
      {
        actionParamKey: "modelRef",
        defaultValue: () => "id:" + model.id,
        readonly: true,
        visible: false,
      },
      {
        actionParamKey: "tagRef",
        defaultValue: () => "id:" + tag.id,
        readonly: true,
        visible: false,
      },
    ],
    displayedSubject,
  );
};
