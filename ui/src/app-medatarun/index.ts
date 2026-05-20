import type { ApplicationConfig } from "@medatarun/ui/app-config";
import { useMedatarunMenu } from "@medatarun/ui/app-medatarun/menu.tsx";

export * from "./menu.tsx";
export * from "./action-registry-static.ts";
export * from "./action-registry-domain-types.ts";
export const applicationConfigMedatarun: ApplicationConfig = {
  applicationName: "Medatarun",
  useApplicationMenu: useMedatarunMenu,
};
