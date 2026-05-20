import { type AnyRoute, createRoute } from "@tanstack/react-router";
import { PreferencesRouteComponent } from "./preferences-route-components.tsx";

export const modulePreferencesRoutes = (rootRoute: AnyRoute) => {
  const preferencesRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/preferences",
    component: PreferencesRouteComponent,
  });

  return [preferencesRoute];
};
