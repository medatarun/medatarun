import "./App.css";
import {
  createRootRoute,
  createRoute,
  createRouter,
  RouterProvider,
  useNavigate,
  useParams,
} from "@tanstack/react-router";
import { ActionRunnerPage } from "@/views/actions/ActionRunnerPage.tsx";
import { ModelListPage } from "@/views/model/ModelListPage.tsx";
import { ModelEditPage } from "@/views/model/ModelEditPage.tsx";
import { EntityEditPage } from "@/views/entity/EntityEditPage.tsx";
import { Layout } from "@/components/layout/layout.tsx";
import { DashboardPage } from "@/views/dashboard/DashboardPage.tsx";
import {
  type ConnectionConfig,
  defaultConnection,
} from "@seij/common-services";
import { SeijUIProvider } from "@seij/common-ui";
import { queryClient } from "@/services/queryClient.ts";
import { QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import {
  AuthenticationCallbackView,
  AuthenticationLoginView,
  AuthenticationLogoutView,
  AuthenticationPaths,
  AuthenticationProvider,
  createAuthenticationConfig,
} from "@seij/common-ui-auth";
import { getOrDefault } from "@/utils/getOrDefault.ts";
import { DetailLevelProvider } from "@/components/business/DetailLevelContext.tsx";
import { PreferencesPage } from "@/views/preferences/PreferencesPage.tsx";
import { AttributeEditPage } from "@/views/attribute/AttributeEditPage.tsx";
import { TypeEditPage } from "@/views/type/TypeEditPage.tsx";
import { RelationshipEditPage } from "@/views/relationship/RelationshipEditPage.tsx";
import { ReportsPage } from "@/views/reports/ReportsPage.tsx";
import { TagGroupListPage } from "@/views/tags/TagGroupListPage.tsx";
import { TagGroupEditPage } from "@/views/tags/TagGroupEditPage.tsx";
import { TagEditPage } from "@/views/tags/TagEditPage.tsx";
import { ModelComparePage } from "@/views/model-compare/ModelComparePage.tsx";
import { ModelHistoryPage } from "@/views/model-history/ModelHistoryPage.tsx";
import { AdminDbDriverListPage } from "@/views/admin-db/AdminDbDriverListPage.tsx";
import { AdminDbDatasourceListPage } from "@/views/admin-db/AdminDbDatasourceListPage.tsx";
import { AdminRoleListPage } from "@/views/admin-roles/AdminRoleListPage.tsx";
import { AdminRoleEditPage } from "@/views/admin-roles/AdminRoleEditPage.tsx";
import { AdminActorListPage } from "@/views/admin-actors/AdminActorListPage.tsx";
import { AdminActorEditPage } from "@/views/admin-actors/AdminActorEditPage.tsx";
import { AdminUserListPage } from "@/views/admin-users/AdminUserListPage.tsx";
import { AdminUserEditPage } from "@/views/admin-users/AdminUserEditPage.tsx";
import { ActionRegistry } from "@/business/action_registry";
import { actionRegistryStatic } from "@/business/action_registry/action_registry.static.ts";
import { ActionPerformer } from "@/business/action-performer";
import { ActionPerformerProvider } from "@/components/business/actions/ActionPerformerProvider.tsx";
import { ActionRegistryContext } from "@/components/business/actions";

function AdminActorListRouteComponent() {
  return <AdminActorListPage />;
}

function AdminActorEditRouteComponent() {
  const { actorId } = useParams({ from: "/admin/actors/$actorId" });
  return <AdminActorEditPage actorId={actorId} />;
}

function AdminDbDatasourceListRouteComponent() {
  return <AdminDbDatasourceListPage />;
}

function AdminDbDriverListRouteComponent() {
  return <AdminDbDriverListPage />;
}

function AdminRoleListRouteComponent() {
  return <AdminRoleListPage />;
}

function AdminRoleEditRouteComponent() {
  const { roleId } = useParams({ from: "/admin/roles/$roleId" });
  return <AdminRoleEditPage roleId={roleId} />;
}

function AdminUserListRouteComponent() {
  return <AdminUserListPage />;
}

function AdminUserEditRouteComponent() {
  const { userId } = useParams({ from: "/admin/users/$userId" });
  return <AdminUserEditPage userId={userId} />;
}

function AuthenticationCallbackComponent() {
  const navigate = useNavigate();
  return (
    <AuthenticationCallbackView onClickHome={() => navigate({ to: "/" })} />
  );
}

function AuthenticationLoginComponent() {
  const navigate = useNavigate();
  return <AuthenticationLoginView onClickHome={() => navigate({ to: "/" })} />;
}

function AuthenticationLogoutComponent() {
  const navigate = useNavigate();
  return <AuthenticationLogoutView onClickHome={() => navigate({ to: "/" })} />;
}

function CommandsRouteComponent() {
  return <ActionRunnerPage />;
}
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function DashboardRouteComponent() {
  return <DashboardPage />;
}

function EntityRouteComponent() {
  const { modelId, entityId } = useParams({
    from: "/model/$modelId/entity/$entityId",
  });
  return <EntityEditPage modelId={modelId} entityId={entityId} />;
}

function EntityAttributeRouteComponent() {
  const { modelId, entityId, attributeId } = useParams({
    from: "/model/$modelId/entity/$entityId/attribute/$attributeId",
  });
  return (
    <AttributeEditPage
      modelId={modelId}
      parentType={"entity"}
      parentId={entityId}
      attributeId={attributeId}
    />
  );
}

function ModelsRouteComponent() {
  const navigate = useNavigate();
  const handleClickModel = (modelId: string) => {
    navigate({ to: "/model/$modelId", params: { modelId } });
  };
  return <ModelListPage onClickModel={handleClickModel} />;
}

function ModelCompareRouteComponent() {
  return <ModelComparePage />;
}

function ModelRouteComponent() {
  const { modelId } = useParams({ from: "/model/$modelId" });
  return <ModelEditPage modelId={modelId} />;
}

function ModelHistoryRouteComponent() {
  const { modelId } = useParams({ from: "/model/$modelId/history" });
  return <ModelHistoryPage modelId={modelId} />;
}

function PreferencesRouteComponent() {
  return <PreferencesPage />;
}

function RelationshipAttributeRouteComponent() {
  const { modelId, relationshipId, attributeId } = useParams({
    from: "/model/$modelId/relationship/$relationshipId/attribute/$attributeId",
  });
  return (
    <AttributeEditPage
      modelId={modelId}
      parentType={"relationship"}
      parentId={relationshipId}
      attributeId={attributeId}
    />
  );
}

function RelationshipRouteComponent() {
  const { modelId, relationshipId } = useParams({
    from: "/model/$modelId/relationship/$relationshipId",
  });
  return (
    <RelationshipEditPage modelId={modelId} relationshipId={relationshipId} />
  );
}

function ReportsRouteComponent() {
  return <ReportsPage />;
}

function TagGroupsRouteComponent() {
  return <TagGroupListPage />;
}

function TagGroupRouteComponent() {
  const { tagGroupId } = useParams({ from: "/tag-group/$tagGroupId" });
  return <TagGroupEditPage tagGroupId={tagGroupId} />;
}

function TagRouteComponent() {
  const { tagId } = useParams({ from: "/tags/$tagId" });
  return <TagEditPage tagId={tagId} />;
}

function TypeRouteComponent() {
  const { modelId, typeId } = useParams({
    from: "/model/$modelId/type/$typeId",
  });
  return <TypeEditPage modelId={modelId} typeId={typeId} />;
}

// Route tree keeps the shared layout and individual pages wired to TanStack Router.
const rootRoute = createRootRoute({
  component: Layout,
});

const adminActorListRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/actors",
  component: AdminActorListRouteComponent,
});

const adminActorEditRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/actors/$actorId",
  component: AdminActorEditRouteComponent,
});

const adminDbDatasourcesRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/db-datasources",
  component: AdminDbDatasourceListRouteComponent,
});
const adminDbDriverRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/db-drivers",
  component: AdminDbDriverListRouteComponent,
});

const adminRoleListRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/roles",
  component: AdminRoleListRouteComponent,
});
const adminRoleEditRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/roles/$roleId",
  component: AdminRoleEditRouteComponent,
});

const adminUserListRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/users",
  component: AdminUserListRouteComponent,
});
const adminUserEditRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/admin/users/$userId",
  component: AdminUserEditRouteComponent,
});

const authenticationLoginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: AuthenticationPaths.login,
  component: AuthenticationLoginComponent,
});
const authenticationLogoutRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: AuthenticationPaths.logout,
  component: AuthenticationLogoutComponent,
});
const authenticationCallbackRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: AuthenticationPaths.callback,
  component: AuthenticationCallbackComponent,
});

const dashboardRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  // component: DashboardRouteComponent,
  component: ModelsRouteComponent,
});

const modelsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/models",
  component: ModelsRouteComponent,
});

const commandsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/commands",
  component: CommandsRouteComponent,
});

const modelRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model/$modelId",
  component: ModelRouteComponent,
});

const modelCompareRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model-compare",
  component: ModelCompareRouteComponent,
});

const modelHistoryRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model/$modelId/history",
  component: ModelHistoryRouteComponent,
});

const preferencesRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/preferences",
  component: PreferencesRouteComponent,
});
const entityRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model/$modelId/entity/$entityId",
  component: EntityRouteComponent,
});

const entityAttributeRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model/$modelId/entity/$entityId/attribute/$attributeId",
  component: EntityAttributeRouteComponent,
});

const relationshipRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model/$modelId/relationship/$relationshipId",
  component: RelationshipRouteComponent,
});

const relationshipAttributeRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model/$modelId/relationship/$relationshipId/attribute/$attributeId",
  component: RelationshipAttributeRouteComponent,
});

const reportsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/reports",
  component: ReportsRouteComponent,
});

const tagGroupsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/tag-groups",
  component: TagGroupsRouteComponent,
});

const tagGroupRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/tag-group/$tagGroupId",
  component: TagGroupRouteComponent,
});

const tagRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/tags/$tagId",
  component: TagRouteComponent,
});

const typeRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/model/$modelId/type/$typeId",
  component: TypeRouteComponent,
});

const routeTree = rootRoute.addChildren([
  adminActorEditRoute,
  adminActorListRoute,
  adminDbDatasourcesRoute,
  adminDbDriverRoute,
  adminRoleEditRoute,
  adminRoleListRoute,
  adminUserEditRoute,
  adminUserListRoute,
  authenticationCallbackRoute,
  authenticationLoginRoute,
  authenticationLogoutRoute,
  commandsRoute,
  dashboardRoute,
  entityAttributeRoute,
  entityRoute,
  modelCompareRoute,
  modelHistoryRoute,
  modelRoute,
  modelsRoute,
  preferencesRoute,
  relationshipAttributeRoute,
  relationshipRoute,
  reportsRoute,
  tagGroupRoute,
  tagGroupsRoute,
  tagRoute,
  typeRoute,
]);

const router = createRouter({
  routeTree,
});

// Register the router instance for type safety
// Do not suppress
declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}

const baseURL = getOrDefault(
  import.meta.env.VITE_BASE_URL,
  window.location.origin,
);

const authenticationConfig = createAuthenticationConfig({
  authority: getOidcAuthority(),
  client_id: getOidcClientId(),
  redirect_uri: baseURL + AuthenticationPaths.callback,
  scope: "openid profile email offline_access",
});

const apiConfig: ConnectionConfig = {
  apiBaseUrl: getOrDefault(import.meta.env.VITE_API_URL, ""),
  getApiAccessToken: authenticationConfig.getCurrentAccessToken,
};

const actionRegistry: ActionRegistry = new ActionRegistry(actionRegistryStatic);

const actionPerformer: ActionPerformer = new ActionPerformer(
  actionRegistry,
  queryClient,
  router.navigate,
);

defaultConnection.reconfigure(apiConfig);

function App() {
  return (
    <SeijUIProvider>
      <ActionRegistryContext.Provider value={actionRegistry}>
        <ActionPerformerProvider performer={actionPerformer}>
          <DetailLevelProvider>
            <AuthenticationProvider {...authenticationConfig}>
              <QueryClientProvider client={queryClient}>
                <RouterProvider router={router} />
                <ReactQueryDevtools initialIsOpen={false} />
              </QueryClientProvider>
            </AuthenticationProvider>
          </DetailLevelProvider>
        </ActionPerformerProvider>
      </ActionRegistryContext.Provider>
    </SeijUIProvider>
  );
}

function getOidcAuthority() {
  const win = window.__MEDATARUN_CONFIG__?.oidcAuthority;
  if (win) {
    console.log("oidcAuthority from window:", win);
    return win;
  }

  const vite = import.meta.env.VITE_OIDC_AUTHORITY;
  if (vite) {
    console.log("oidcAuthority from Vite config:", vite);
    return vite;
  }

  return "http://localhost:8080";
}

function getOidcClientId() {
  const win = window.__MEDATARUN_CONFIG__?.oidcClientId;
  if (win) {
    console.log("oidcAuthority from window:", win);
    return win;
  }

  const vite = import.meta.env.VITE_OIDC_CLIENT_ID;
  if (vite) {
    console.log("oidcAuthority from Vite config:", vite);
    return vite;
  }

  return "medatarun-ui";
}

export default App;
