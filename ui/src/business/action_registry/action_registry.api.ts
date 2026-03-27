import type { ActionRegistryDto } from "./action_registry.dto.ts";
import { executeAction } from "@/business/action_runner";
import { useQuery } from "@tanstack/react-query";
import { ActionRegistry } from "./action_registry.biz.tsx";

export async function fetchActionDescriptors(): Promise<ActionRegistryDto> {
  const response = await executeAction<ActionRegistryDto>(
    "config",
    "inspect_actions",
    {},
  );
  if (response.contentType !== "json") {
    return { items: [] };
  }
  return response.json;
}
