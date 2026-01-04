import {Outlet, useMatchRoute, useNavigate} from "@tanstack/react-router";
import {MessageBar} from "@fluentui/react-components";
import {useEffect, useState} from "react";
import {ActionRegistry, ActionsContext, fetchActionDescriptors} from "../../business";
import {ActionPerformerView} from "../business/ActionPerformerView.tsx";
import {ActionProvider} from "../business/ActionPerformerProvider.tsx";
import logo from "../../../public/favicon/favicon.svg"
import {ErrorBoundary} from "./ErrorBoundary.tsx";
import {ApplicationShell, Loader, type NavigationTreeItem, type UserStatus} from "@seij/common-ui";


export function Layout2() {
  const [actions, setActions] = useState<ActionRegistry>()
  const [error, setError] = useState<any | null>(null)
  const navigate = useNavigate()
  const userStatus: UserStatus = {
    isAuthenticated: false,
    isLoading: false,
    errorMessage: null,
    onClickSignIn: () => {
    },
    onClickSignOut: () => {
    },
    userName: null
  }
  const matchRoute = useMatchRoute()

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
  const navigationItems: NavigationTreeItem[] = [
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
    }
  ]
  return <ApplicationShell
    applicationName={"Medatarun"}
    applicationIcon={<img src={logo} alt="Medatarun logo" style={{
      width: "2em",
      height: "2em"
    }}/>}
    main={
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
        {!actions && <Loader loading={true} /> }
        {
          error && <div>
            <p>Sorry, we could not load this page.</p>
            <MessageBar intent="error">{error.toString()}</MessageBar>
          </div>
        }
      </>
    }
    navigate={path => navigate({to: path})}
    onClickHome={() => navigate({to: "/"})}
    userStatus={userStatus}
    navigationItems={navigationItems}
    matchPath={matchPath}
  >

  </ApplicationShell>
}