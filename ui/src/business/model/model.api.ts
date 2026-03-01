import { api } from "@/services/api.ts";
import type { ModelDto, ModelSummaryDto } from "./model.dto.ts";

export async function fetchModelSummaries(): Promise<ModelSummaryDto[]> {
  return api().get<ModelSummaryDto[]>("/ui/api/models");
}
export async function fetchModel(modelKey: string): Promise<ModelDto> {
  return api().get<ModelDto>("/ui/api/models/" + modelKey);
}
