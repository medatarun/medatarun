import './App.css'
import {
  createRootRoute,
  createRoute,
  createRouter,
  Link,
  Outlet,
  RouterProvider,
  useNavigate,
  useParams,
} from '@tanstack/react-router'
import {CommandsPage} from "./components/CommandsPage.tsx";
import {ModelsPage} from "./components/ModelsPage.tsx";
import {ModelPage} from "./components/ModelPage.tsx";
import {EntityPage} from "./components/EntityPage.tsx";

function Layout() {
  return <div>
    <nav className="menubar">
      <div>Medatarun</div>
      <div><Link to="/">Home</Link></div>
      <div><Link to="/">Models</Link></div>
      <div><Link to="/commands">Commands</Link></div>
    </nav>
    <main className="container"><Outlet/></main>
  </div>
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
  return <EntityPage modelId={modelId} entityDefId={entityDefId} />
}

// Route tree keeps the shared layout and individual pages wired to TanStack Router.
const rootRoute = createRootRoute({
  component: Layout,
});

const modelsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
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
  modelsRoute, commandsRoute, modelRoute, entityRoute
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

function App() {
  return (
    <RouterProvider router={router}/>
  )
}

export default App
