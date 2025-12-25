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
import {FluentProvider, webLightTheme} from '@fluentui/react-components';
import {Layout} from "./components/layout/layout.tsx";
import {DashboardPage} from "./views/DashboardPage.tsx";
import {defaultConnection} from "@seij/common-services";

function DashboardRouteComponent() {
  return <DashboardPage />
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

// Route tree keeps the shared layout and individual pages wired to TanStack Router.
const rootRoute = createRootRoute({
  component: Layout,
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
const routeTree = rootRoute.addChildren([
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

function App() {
  return (
    <FluentProvider theme={webLightTheme}>
      <RouterProvider router={router}/>
    </FluentProvider>
  )
}

export default App
