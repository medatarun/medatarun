import type { ActionRegistryDto } from "./action_registry.dto.ts";
import { executeActionJson } from "@/business/action_runner";

export async function fetchActionDescriptors(): Promise<ActionRegistryDto> {
  return executeActionJson<ActionRegistryDto>("config", "inspect_actions", {});
}
