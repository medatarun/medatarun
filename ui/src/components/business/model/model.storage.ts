import { useMutation, useQuery } from "@tanstack/react-query";
import type {
  ModelChangeEventListDto,
  ModelChangeEventListWithVersionDto,
  ModelCompareDto,
  ModelCompareReq,
  ModelDto,
  ModelSearchFilter,
  ModelSearchOperator,
  ModelListItemDto,
  SearchResults,
} from "@medatarun/ui/business/model";
import {
  type ActionPayload,
  ActionPerformer,
} from "@medatarun/ui/business/action-performer";
import { api } from "@medatarun/ui/services/api.ts";
import { useActionPerformer } from "@medatarun/ui/components/business/actions/action-performer-hook.tsx";
import type { ActionKey } from "../../../business/action-registry";

export type ModelSearchReq = {
  operator: ModelSearchOperator;
  items: ModelSearchFilter[];
};

export async function modelSearch(
  performer: ActionPerformer,
  req: ModelSearchReq,
): Promise<SearchResults> {
  if (req.items.length === 0) return { items: [] };
  const payload = {
    filters: {
      operator: req.operator,
      items: req.items.map((item) => {
        if (item.type === "tags") {
          if (item.condition === "empty" || item.condition === "notEmpty") {
            return {
              type: "tags",
              condition: item.condition,
            };
          }
          return {
            type: "tags",
            condition: item.condition,
            value: item.tagIds.map((tagId) => "id:" + tagId),
          };
        }
        return {
          type: "text",
          condition: item.condition,
          value: item.value,
        };
      }),
    },
    fields: ["location"],
  };
  return performer.executeJson<SearchResults>("models/search", payload);
}
export const useModelSearch = (req: ModelSearchReq) => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["search", req],
    queryFn: () => modelSearch(performer, req),
  });
};

export const useModelCompare = (req: ModelCompareReq | undefined) => {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["model", "compare", req],
    queryFn: async () => {
      if (req === undefined) return undefined;
      return performer.executeJson<ModelCompareDto>("models/model_compare", {
        leftModelRef: "id:" + req.leftModelId,
        leftModelVersion: req.leftModelVersion,
        rightModelRef: "id:" + req.rightModelId,
        rightModelVersion: req.rightModelVersion,
        scope: req.scope,
      });
    },
  });
};

export function useModelHistoryVersions(modelId: string) {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["model", modelId, "history_versions"],
    queryFn: () =>
      performer.executeJson<ModelChangeEventListWithVersionDto>(
        "models/history_versions",
        {
          modelRef: "id:" + modelId,
        },
      ),
    enabled: modelId.length > 0,
  });
}

export function useModelHistoryVersionChanges(
  modelId: string,
  version: string | null,
) {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["model", modelId, "history_version_changes", version],
    queryFn: () => {
      const payload: ActionPayload = {
        modelRef: "id:" + modelId,
      };
      if (version !== null) {
        payload.version = version;
      }
      return performer.executeJson<ModelChangeEventListDto>(
        "models/history_version_changes",
        payload,
      );
    },
  });
}

export const useModelUpdateName = () => {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      performer.executeJson("models/model_update_name", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelUpdateDescription = () => {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      performer.executeJson("models/model_update_description", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelUpdateDocumentationHome = () => {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      performer.executeJson("models/model_update_documentation_link", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelUpdateKey = () => {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      performer.executeJson("models/model_update_key", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelAddTag = () => {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; tag: string }) =>
      performer.executeJson("models/model_add_tag", {
        modelRef: "id:" + props.modelId,
        tag: props.tag,
      }),
  });
};
export const useModelDeleteTag = () => {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; tag: string }) =>
      performer.executeJson("models/model_delete_tag", {
        modelRef: "id:" + props.modelId,
        tag: props.tag,
      }),
  });
};

function useTypeMutation<P extends ActionPayload>(actionRef: ActionKey) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; typeId: string } & P) => {
      const { modelId, typeId, ...otherProps } = props;
      return performer.executeJson(actionRef, {
        modelRef: "id:" + modelId,
        typeRef: "id:" + typeId,
        ...otherProps,
      });
    },
  });
}

export const useTypeUpdateName = () => {
  return useTypeMutation<{ value: string }>("models/type_update_name");
};
export const useTypeUpdateKey = () => {
  return useTypeMutation<{ value: string }>("models/type_update_key");
};
export const useTypeUpdateDescription = () => {
  return useTypeMutation<{ value: string }>("models/type_update_description");
};

function useEntityMutation<P extends ActionPayload>(actionRef: ActionKey) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; entityId: string } & P) => {
      const { modelId, entityId, ...otherProps } = props;
      return performer.executeJson(actionRef, {
        modelRef: "id:" + modelId,
        entityRef: "id:" + entityId,
        ...otherProps,
      });
    },
  });
}

export const useEntityUpdateName = () => {
  return useEntityMutation<{ value: string }>("models/entity_update_name");
};
export const useEntityUpdateKey = () => {
  return useEntityMutation<{ value: string }>("models/entity_update_key");
};
export const useEntityUpdateDescription = () => {
  return useEntityMutation<{ value: string }>(
    "models/entity_update_description",
  );
};
export const useEntityUpdateDocumentationHome = () => {
  return useEntityMutation<{ value: string }>(
    "models/entity_update_documentation_link",
  );
};
export const useEntityAddTag = () => {
  return useEntityMutation<{ tag: string }>("models/entity_add_tag");
};
export const useEntityDeleteTag = () => {
  return useEntityMutation<{ tag: string }>("models/entity_delete_tag");
};

function useEntityAttributeMutation<P extends ActionPayload>(
  actionRef: ActionKey,
) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (
      props: { modelId: string; entityId: string; attributeId: string } & P,
    ) => {
      const { modelId, entityId, attributeId, ...otherProps } = props;
      return performer.executeJson(actionRef, {
        modelRef: "id:" + modelId,
        entityRef: "id:" + entityId,
        attributeRef: "id:" + attributeId,
        ...otherProps,
      });
    },
  });
}

export const useEntityAttributeUpdateName = () => {
  return useEntityAttributeMutation<{ value: string }>(
    "models/entity_attribute_update_name",
  );
};
export const useEntityAttributeUpdateDescription = () => {
  return useEntityAttributeMutation<{ value: string }>(
    "models/entity_attribute_update_description",
  );
};
export const useEntityAttributeUpdateKey = () => {
  return useEntityAttributeMutation<{ value: string }>(
    "models/entity_attribute_update_key",
  );
};
export const useEntityAttributeUpdateType = () => {
  return useEntityAttributeMutation<{ value: string }>(
    "models/entity_attribute_update_type",
  );
};
export const useEntityAttributeUpdateOptional = () => {
  return useEntityAttributeMutation<{ value: boolean }>(
    "models/entity_attribute_update_optional",
  );
};
export const useEntityAttributeAddTag = () => {
  return useEntityAttributeMutation<{ tag: string }>(
    "models/entity_attribute_add_tag",
  );
};
export const useEntityAttributeDeleteTag = () => {
  return useEntityAttributeMutation<{ tag: string }>(
    "models/entity_attribute_delete_tag",
  );
};

function useRelationshipMutation<P extends ActionPayload>(
  actionRef: ActionKey,
) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (props: { modelId: string; relationshipId: string } & P) => {
      const { modelId, relationshipId, ...otherProps } = props;
      return performer.executeJson(actionRef, {
        modelRef: "id:" + modelId,
        relationshipRef: "id:" + relationshipId,
        ...otherProps,
      });
    },
  });
}

export const useRelationshipUpdateName = () => {
  return useRelationshipMutation<{ value: string }>(
    "models/relationship_update_name",
  );
};
export const useRelationshipUpdateDescription = () => {
  return useRelationshipMutation<{ value: string }>(
    "models/relationship_update_description",
  );
};
export const useRelationshipUpdateKey = () => {
  return useRelationshipMutation<{ value: string }>(
    "models/relationship_update_key",
  );
};
export const useRelationshipAddTag = () => {
  return useRelationshipMutation<{ tag: string }>(
    "models/relationship_add_tag",
  );
};
export const useRelationshipDeleteTag = () => {
  return useRelationshipMutation<{ tag: string }>(
    "models/relationship_delete_tag",
  );
};

function useRelationshipAttributeMutation<P extends ActionPayload>(
  actionRef: ActionKey,
) {
  const { performer } = useActionPerformer();
  return useMutation({
    mutationFn: (
      props: {
        modelId: string;
        relationshipId: string;
        attributeId: string;
      } & P,
    ) => {
      const { modelId, relationshipId, attributeId, ...otherProps } = props;
      return performer.executeJson(actionRef, {
        modelRef: "id:" + modelId,
        relationshipRef: "id:" + relationshipId,
        attributeRef: "id:" + attributeId,
        ...otherProps,
      });
    },
  });
}

export const useRelationshipAttributeUpdateName = () => {
  return useRelationshipAttributeMutation<{ value: string }>(
    "models/relationship_attribute_update_name",
  );
};
export const useRelationshipAttributeUpdateDescription = () => {
  return useRelationshipAttributeMutation<{ value: string }>(
    "models/relationship_attribute_update_description",
  );
};
export const useRelationshipAttributeUpdateKey = () => {
  return useRelationshipAttributeMutation<{ value: string }>(
    "models/relationship_attribute_update_key",
  );
};
export const useRelationshipAttributeUpdateType = () => {
  return useRelationshipAttributeMutation<{ value: string }>(
    "models/relationship_attribute_update_type",
  );
};
export const useRelationshipAttributeUpdateOptional = () => {
  return useRelationshipAttributeMutation<{ value: boolean }>(
    "models/relationship_attribute_update_optional",
  );
};
export const useRelationshipAttributeAddTag = () => {
  return useRelationshipAttributeMutation<{ tag: string }>(
    "models/relationship_attribute_add_tag",
  );
};
export const useRelationshipAttributeDeleteTag = () => {
  return useRelationshipAttributeMutation<{ tag: string }>(
    "models/relationship_attribute_delete_tag",
  );
};

export interface ModelListDto {
  items: ModelListItemDto[];
}

export function useModelList() {
  const { performer } = useActionPerformer();
  return useQuery({
    queryKey: ["model", "list"],
    queryFn: () => performer.executeJson<ModelListDto>("models/model_list", {}),
  });
}

/* FIXME: do not use api() directly, use actions */
export function useModel(modelId: string) {
  return useQuery({
    queryKey: ["model", "aggregate", modelId],
    queryFn: () => api().get<ModelDto>("/ui/api/models/" + modelId),
  });
}
