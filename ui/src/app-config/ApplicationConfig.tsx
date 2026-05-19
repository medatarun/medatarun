import type { NavigationTreeItem } from "@seij/common-ui";
import { createContext, useContext } from "react";

export interface ApplicationConfig {
  applicationName: string;
  /**
   * Hook called by the shared layout to get the menu for the host application.
   *
   * Implementations can use React hooks, so the layout must call this
   * unconditionally during render.
   */
  useApplicationMenu: () => NavigationTreeItem[];
}

export const ApplicationConfigContext = createContext<ApplicationConfig>({
  applicationName: "Unknown",
  useApplicationMenu: () => [],
});

export function useApplicationConfig() {
  const ctx = useContext(ApplicationConfigContext);
  return ctx;
}
