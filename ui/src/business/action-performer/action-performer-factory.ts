import { ActionPerformer } from "./ActionPerformer.ts";
import { ActionRegistryInstance } from "@/business/action_registry";
import { queryClient } from "@/services/queryClient.ts";

export const ActionPerformerInstance: ActionPerformer = new ActionPerformer(
  ActionRegistryInstance,
  queryClient,
);
