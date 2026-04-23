import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchModel, fetchModelSummaries } from "./model.api.ts";
import type {
  ModelChangeEventListDto,
  ModelChangeEventListWithVersionDto,
  ModelCompareDto,
  SearchResults,
} from "./model.dto.ts";
import { type ActionPayload, executeActionJson } from "../action_runner";

export type ModelSearchOperator = "and" | "or";

export type ModelSearchTagFilterCondition =
  | "anyOf"
  | "allOf"
  | "noneOf"
  | "empty"
  | "notEmpty";

export type ModelSearchTagFilter = {
  id: string;
  type: "tags";
  condition: ModelSearchTagFilterCondition;
  tagIds: string[];
};

export type ModelSearchTextFilter = {
  id: string;
  type: "text";
  condition: "contains";
  value: string;
};

export type ModelSearchFilter = ModelSearchTagFilter | ModelSearchTextFilter;

export type ModelSearchReq = {
  operator: ModelSearchOperator;
  items: ModelSearchFilter[];
};

export async function modelSearch(req: ModelSearchReq): Promise<SearchResults> {
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
  return executeActionJson<SearchResults>("model", "search", payload);
}
export const useModelSearch = (req: ModelSearchReq) => {
  return useQuery({
    queryKey: ["search", req],
    queryFn: () => modelSearch(req),
  });
};

export type ModelDiffScopeCode = "structural" | "complete";

export type ModelCompareReq = {
  leftModelId: string;
  leftModelVersion: string | null;
  rightModelId: string;
  rightModelVersion: string | null;
  scope: ModelDiffScopeCode;
};

export async function modelCompare(
  req: ModelCompareReq,
): Promise<ModelCompareDto> {
  return executeActionJson<ModelCompareDto>("model", "model_compare", {
    leftModelRef: "id:" + req.leftModelId,
    leftModelVersion: req.leftModelVersion,
    rightModelRef: "id:" + req.rightModelId,
    rightModelVersion: req.rightModelVersion,
    scope: req.scope,
  });
}

export const useModelCompare = () => {
  return useMutation({
    mutationFn: (req: ModelCompareReq) => modelCompare(req),
  });
};

export async function modelHistoryVersions(
  modelId: string,
): Promise<ModelChangeEventListWithVersionDto> {
  return executeActionJson<ModelChangeEventListWithVersionDto>(
    "model",
    "history_versions",
    {
      modelRef: "id:" + modelId,
    },
  );
}

export async function modelHistoryVersionChanges(
  modelId: string,
  version: string | null,
): Promise<ModelChangeEventListDto> {
  const payload: ActionPayload = {
    modelRef: "id:" + modelId,
  };
  if (version !== null) {
    payload.version = version;
  }
  return executeActionJson<ModelChangeEventListDto>(
    "model",
    "history_version_changes",
    payload,
  );
}

export function useModelHistoryVersions(modelId: string) {
  return useQuery({
    queryKey: ["model", modelId, "history_versions"],
    queryFn: () => modelHistoryVersions(modelId),
    enabled: modelId.length > 0,
  });
}

export function useModelHistoryVersionChanges(
  modelId: string,
  version: string | null,
) {
  return useQuery({
    queryKey: ["model", modelId, "history_version_changes", version],
    queryFn: () => modelHistoryVersionChanges(modelId, version),
  });
}

export const useModelUpdateName = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      executeActionJson("model", "model_update_name", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelUpdateDescription = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      executeActionJson("model", "model_update_description", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelUpdateDocumentationHome = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      executeActionJson("model", "model_update_documentation_link", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelUpdateKey = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; value: string }) =>
      executeActionJson("model", "model_update_key", {
        modelRef: "id:" + props.modelId,
        value: props.value,
      }),
  });
};
export const useModelAddTag = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; tag: string }) =>
      executeActionJson("model", "model_add_tag", {
        modelRef: "id:" + props.modelId,
        tag: props.tag,
      }),
  });
};
export const useModelDeleteTag = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; tag: string }) =>
      executeActionJson("model", "model_delete_tag", {
        modelRef: "id:" + props.modelId,
        tag: props.tag,
      }),
  });
};

function useTypeMutation<P extends ActionPayload>(
  actionGroupKey: string,
  actionKey: string,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; typeId: string } & P) => {
      const { modelId, typeId, ...otherProps } = props;
      return executeActionJson(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        typeRef: "id:" + typeId,
        ...otherProps,
      });
    },
  });
}

export const useTypeUpdateName = () => {
  return useTypeMutation<{ value: string }>("model", "type_update_name");
};
export const useTypeUpdateKey = () => {
  return useTypeMutation<{ value: string }>("model", "type_update_key");
};
export const useTypeUpdateDescription = () => {
  return useTypeMutation<{ value: string }>("model", "type_update_description");
};

function useEntityMutation<P extends ActionPayload>(
  actionGroupKey: string,
  actionKey: string,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; entityId: string } & P) => {
      const { modelId, entityId, ...otherProps } = props;
      return executeActionJson(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        entityRef: "id:" + entityId,
        ...otherProps,
      });
    },
  });
}

export const useEntityUpdateName = () => {
  return useEntityMutation<{ value: string }>("model", "entity_update_name");
};
export const useEntityUpdateKey = () => {
  return useEntityMutation<{ value: string }>("model", "entity_update_key");
};
export const useEntityUpdateDescription = () => {
  return useEntityMutation<{ value: string }>(
    "model",
    "entity_update_description",
  );
};
export const useEntityUpdateDocumentationHome = () => {
  return useEntityMutation<{ value: string }>(
    "model",
    "entity_update_documentation_link",
  );
};
export const useEntityAddTag = () => {
  return useEntityMutation<{ tag: string }>("model", "entity_add_tag");
};
export const useEntityDeleteTag = () => {
  return useEntityMutation<{ tag: string }>("model", "entity_delete_tag");
};

function useEntityAttributeMutation<P extends ActionPayload>(
  actionGroupKey: string,
  actionKey: string,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (
      props: { modelId: string; entityId: string; attributeId: string } & P,
    ) => {
      const { modelId, entityId, attributeId, ...otherProps } = props;
      return executeActionJson(actionGroupKey, actionKey, {
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
    "model",
    "entity_attribute_update_name",
  );
};
export const useEntityAttributeUpdateDescription = () => {
  return useEntityAttributeMutation<{ value: string }>(
    "model",
    "entity_attribute_update_description",
  );
};
export const useEntityAttributeUpdateKey = () => {
  return useEntityAttributeMutation<{ value: string }>(
    "model",
    "entity_attribute_update_key",
  );
};
export const useEntityAttributeUpdateType = () => {
  return useEntityAttributeMutation<{ value: string }>(
    "model",
    "entity_attribute_update_type",
  );
};
export const useEntityAttributeUpdateOptional = () => {
  return useEntityAttributeMutation<{ value: boolean }>(
    "model",
    "entity_attribute_update_optional",
  );
};
export const useEntityAttributeAddTag = () => {
  return useEntityAttributeMutation<{ tag: string }>(
    "model",
    "entity_attribute_add_tag",
  );
};
export const useEntityAttributeDeleteTag = () => {
  return useEntityAttributeMutation<{ tag: string }>(
    "model",
    "entity_attribute_delete_tag",
  );
};

function useRelationshipMutation<P extends ActionPayload>(
  actionGroupKey: string,
  actionKey: string,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (props: { modelId: string; relationshipId: string } & P) => {
      const { modelId, relationshipId, ...otherProps } = props;
      return executeActionJson(actionGroupKey, actionKey, {
        modelRef: "id:" + modelId,
        relationshipRef: "id:" + relationshipId,
        ...otherProps,
      });
    },
  });
}

export const useRelationshipUpdateName = () => {
  return useRelationshipMutation<{ value: string }>(
    "model",
    "relationship_update_name",
  );
};
export const useRelationshipUpdateDescription = () => {
  return useRelationshipMutation<{ value: string }>(
    "model",
    "relationship_update_description",
  );
};
export const useRelationshipUpdateKey = () => {
  return useRelationshipMutation<{ value: string }>(
    "model",
    "relationship_update_key",
  );
};
export const useRelationshipAddTag = () => {
  return useRelationshipMutation<{ tag: string }>(
    "model",
    "relationship_add_tag",
  );
};
export const useRelationshipDeleteTag = () => {
  return useRelationshipMutation<{ tag: string }>(
    "model",
    "relationship_delete_tag",
  );
};

function useRelationshipAttributeMutation<P extends ActionPayload>(
  actionGroupKey: string,
  actionKey: string,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (
      props: {
        modelId: string;
        relationshipId: string;
        attributeId: string;
      } & P,
    ) => {
      const { modelId, relationshipId, attributeId, ...otherProps } = props;
      return executeActionJson(actionGroupKey, actionKey, {
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
    "model",
    "relationship_attribute_update_name",
  );
};
export const useRelationshipAttributeUpdateDescription = () => {
  return useRelationshipAttributeMutation<{ value: string }>(
    "model",
    "relationship_attribute_update_description",
  );
};
export const useRelationshipAttributeUpdateKey = () => {
  return useRelationshipAttributeMutation<{ value: string }>(
    "model",
    "relationship_attribute_update_key",
  );
};
export const useRelationshipAttributeUpdateType = () => {
  return useRelationshipAttributeMutation<{ value: string }>(
    "model",
    "relationship_attribute_update_type",
  );
};
export const useRelationshipAttributeUpdateOptional = () => {
  return useRelationshipAttributeMutation<{ value: boolean }>(
    "model",
    "relationship_attribute_update_optional",
  );
};
export const useRelationshipAttributeAddTag = () => {
  return useRelationshipAttributeMutation<{ tag: string }>(
    "model",
    "relationship_attribute_add_tag",
  );
};
export const useRelationshipAttributeDeleteTag = () => {
  return useRelationshipAttributeMutation<{ tag: string }>(
    "model",
    "relationship_attribute_delete_tag",
  );
};

export function useModelSummaries() {
  return useQuery({
    queryKey: ["model_summaries"],
    queryFn: fetchModelSummaries,
  });
}

export function useModel(modelId: string) {
  return useQuery({
    queryKey: ["model", modelId],
    queryFn: () => fetchModel(modelId),
  });
}
