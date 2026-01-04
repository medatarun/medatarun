import type {ActionRegistryDto} from "./action_registry.dto.ts";
import {api} from "../services/api.ts";

export async function fetchActionDescriptors(): Promise<ActionRegistryDto> {
  return api().get<ActionRegistryDto>("/ui/api/action-registry")
}
