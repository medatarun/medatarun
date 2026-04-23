import { ActionPerformer } from "@/business/action-performer/ActionPerformer.ts";
import { ActionRegistryInstance } from "@/business/action_registry";
import {
  ActionPostHookCompat,
  ActionPostHooks,
} from "@/business/action-performer/ActionPostHook.ts";
import { queryClient } from "@/services/queryClient.ts";

export const ActionPerformerInstance: ActionPerformer = new ActionPerformer(
  ActionRegistryInstance,
  new ActionPostHooks([
    new ActionPostHookCompat(ActionRegistryInstance, queryClient),
  ]),
);
