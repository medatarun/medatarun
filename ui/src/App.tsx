import './App.css'
import {
  createRootRoute,
  createRoute,
  createRouter,
  RouterProvider,
  useNavigate,
  useParams,
} from '@tanstack/react-router'
import {CommandsPage} from "./views/CommandsPage.tsx";
import {ModelsPage} from "./views/ModelsPage.tsx";
import {ModelPage} from "./views/ModelPage.tsx";
import {EntityPage} from "./views/EntityPage.tsx";
import {Layout2} from "./components/layout/layout.tsx";
import {DashboardPage} from "./views/DashboardPage.tsx";
import {type ConnectionConfig, defaultConnection} from "@seij/common-services";
import {SeijUIProvider} from "@seij/common-ui";
import {queryClient} from "./services/queryClient.ts";
import {QueryClientProvider} from "@tanstack/react-query";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {
  AuthenticationCallbackView,
  AuthenticationLoginView,
  AuthenticationLogoutView,
  AuthenticationPaths,
  AuthenticationProvider,
  createAuthenticationConfig
} from "@seij/common-ui-auth";
import {getOrDefault} from "./utils/getOrDefault.ts";

function DashboardRouteComponent() {
  return <DashboardPage/>
}

function ModelsRouteComponent() {
  const navigate = useNavigate();
  const handleClickModel = (modelId: string) => {
    navigate({to: '/model/$modelId', params: {modelId}});
  };
  return <ModelsPage onClickModel={handleClickModel}/>
}

function CommandsRouteComponent() {
  return <CommandsPage/>
}

function ModelRouteComponent() {
  const {modelId} = useParams({from: '/model/$modelId'});
  return <ModelPage modelId={modelId}/>
}

function EntityDefRouteComponent() {
  const {modelId, entityDefId} = useParams({from: '/model/$modelId/entityDef/$entityDefId'});
  return <EntityPage modelId={modelId} entityDefId={entityDefId}/>
}

function AuthenticationCallbackComponent() {
  const navigate = useNavigate();
  return <AuthenticationCallbackView onClickHome={()=>navigate({to:"/"})} />
}

function AuthenticationLoginComponent() {
  const navigate = useNavigate();
  return <AuthenticationLoginView onClickHome={()=>navigate({to:"/"})} />
}

function AuthenticationLogoutComponent() {
  const navigate = useNavigate();
  return <AuthenticationLogoutView onClickHome={()=>navigate({to:"/"})} />
}

// Route tree keeps the shared layout and individual pages wired to TanStack Router.
const rootRoute = createRootRoute({
  component: Layout2,
});

const dashboardRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: DashboardRouteComponent,
})

const modelsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/models',
  component: ModelsRouteComponent,
});

const commandsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/commands',
  component: CommandsRouteComponent,
});

const modelRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/model/$modelId',
  component: ModelRouteComponent,
});
const entityRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/model/$modelId/entityDef/$entityDefId',
  component: EntityDefRouteComponent
})
const authenticationLoginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: AuthenticationPaths.login,
  component: AuthenticationLoginComponent,
})
const authenticationLogoutRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: AuthenticationPaths.logout,
  component: AuthenticationLogoutComponent,
})
const authenticationCallbackRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: AuthenticationPaths.callback,
  component: AuthenticationCallbackComponent,
})

const routeTree = rootRoute.addChildren([
  authenticationLoginRoute,
  authenticationLogoutRoute,
  authenticationCallbackRoute,
  dashboardRoute, modelsRoute, commandsRoute, modelRoute, entityRoute
]);

const router = createRouter({
  routeTree,
});

// Register the router instance for type safety
// Do not suppress
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

const services = defaultConnection
console.log("Services", services)

const baseURL = getOrDefault(import.meta.env.VITE_BASE_URL, window.location.origin);

const authenticationConfig = createAuthenticationConfig({
  authority: getOrDefault(import.meta.env.VITE_OIDC_AUTHORITY, "http://localhost:8080"),
  client_id: getOrDefault(import.meta.env.VITE_OIDC_CLIENT_ID, "medatarun-ui"),
  redirect_uri: baseURL + AuthenticationPaths.callback,
});

const apiConfig: ConnectionConfig = {
  apiBaseUrl: getOrDefault(import.meta.env.VITE_API_URL, ""),
  getApiAccessToken: authenticationConfig.getCurrentAccessToken
};
defaultConnection.reconfigure(apiConfig);

function App() {
  return (
    <SeijUIProvider>
      <AuthenticationProvider {...authenticationConfig}>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router}/>
        <ReactQueryDevtools initialIsOpen={false} />
      </QueryClientProvider>
      </AuthenticationProvider>
    </SeijUIProvider>
  )
}

export default App
