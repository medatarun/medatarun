import { type AnyRoute, createRoute } from "@tanstack/react-router";
import { CommandsRouteComponent } from "./actions-route-components.tsx";

export const moduleActionsRoutes = (rootRoute: AnyRoute) => {
  const commandsRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/commands",
    component: CommandsRouteComponent,
  });

  return [commandsRoute];
};
