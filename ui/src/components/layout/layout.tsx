import {Outlet, useLocation, useMatchRoute, useNavigate} from "@tanstack/react-router";
import {MessageBar} from "@fluentui/react-components";
import {useEffect, useState} from "react";
import {ActionRegistry, ActionsContext, fetchActionDescriptors} from "../../business";
import {ActionPerformerView} from "../business/ActionPerformerView.tsx";
import {ActionProvider} from "../business/ActionPerformerProvider.tsx";
import logo from "../../../public/favicon/favicon.svg"
import {ErrorBoundary} from "./ErrorBoundary.tsx";
import {Loader, type NavigationTreeItem} from "@seij/common-ui";
import {ApplicationShellSecured} from "@seij/common-ui-auth";
import {useDetailLevelContext} from "../business/DetailLevelContext.tsx";


export function Layout2() {
  const [actions, setActions] = useState<ActionRegistry>()
  const [error, setError] = useState<any | null>(null)
  const navigate = useNavigate()
  const matchRoute = useMatchRoute()
  const {pathname} = useLocation()
  const {isDetailLevelTech} = useDetailLevelContext()

  useEffect(() => {
    fetchActionDescriptors()
      .then(dto => {
        const ar = new ActionRegistry(dto)
        setActions(ar)
      })
      .catch(err => {
        setError(err)
        console.log(err)
      })
  }, [])

  const matchPath = (path: string | undefined) => !!matchRoute({to: path, fuzzy: true})
  const navigationItemsBase: NavigationTreeItem[] = [
    {
      id: "home",
      parentId: null,
      type: "page",
      path: "/",
      label: "Home",
      description: undefined,
      icon: "dashboard",
      rule: undefined
    },
    {
      id: "models",
      parentId: null,
      type: "page",
      path: "/models",
      label: "Models",
      description: undefined,
      icon: "dashboard",
      rule: undefined
    },
    {
      id: "commands",
      parentId: null,
      type: "page",
      path: "/commands",
      label: "Commands",
      description: undefined,
      icon: "dashboard",
      rule: undefined
    },{
      id: "reports",
      parentId: null,
      type: "page",
      path: "/reports",
      label: "Reports",
      description: undefined,
      icon: "dashboard",
      rule: undefined
    },
    {
      id: "preferences",
      parentId: null,
      type: "page",
      path: "/preferences",
      label: "Preferences",
      description: undefined,
      icon: "dashboard",
      rule: undefined
    }
  ]
  const nav = navigationItemsBase.filter(it => it.id !== "commands" || isDetailLevelTech)


  return <ApplicationShellSecured
    applicationName={"Medatarun"}
    applicationIcon={<img src={logo} alt="Medatarun logo" style={{
      width: "2em",
      height: "2em"
    }}/>}
    pathname={pathname}
    outlet={
      <>
        {actions &&
          <ActionsContext value={actions}>
            <ActionProvider>
              <ErrorBoundary>
                <Outlet/>
                <ActionPerformerView/>
              </ErrorBoundary>
            </ActionProvider>
          </ActionsContext>
        }
        {!actions && <Loader loading={true}/>}
        {
          error && <div>
            <p>Sorry, we could not load this page.</p>
            <MessageBar intent="error">{JSON.stringify(error)}</MessageBar>
          </div>
        }
      </>
    }
    navigate={path => navigate({to: path})}
    onClickHome={() => navigate({to: "/"})}
    navigationItems={nav}
    matchPath={matchPath}
  >

  </ApplicationShellSecured>
}