import {Outlet, useLocation, useMatchRoute, useNavigate,} from "@tanstack/react-router";
import {useEffect, useMemo, useRef, useState} from "react";
import {ActionRegistry, ActionsContext, fetchActionDescriptors,} from "@/business/action_registry";
import {ActionPerformerView} from "@/components/business/actions/ActionPerformerView.tsx";
import {ActionProvider} from "@/components/business/actions/ActionPerformerProvider.tsx";
import logo from "../../../public/favicon/favicon.svg";
import {ErrorBoundary} from "./ErrorBoundary.tsx";
import {ErrorBox, Loader, type NavigationTreeItem} from "@seij/common-ui";
import {ApplicationShellSecured, useAuthentication} from "@seij/common-ui-auth";
import {useDetailLevelContext} from "@/components/business/DetailLevelContext.tsx";
import {UnauthorizedHandler} from "@/components/auth/UnauthorizedHandler.tsx";
import {useAppI18n} from "@/services/appI18n.tsx";
import {modelActionPostHook} from "@/business/model";
import {tagActionPostHook} from "@/business/tag";
import {ActionPostHooks} from "@/components/business/actions/ActionPostHook.ts";
import {type Problem, toProblem} from "@seij/common-types";

const EMPTY_ACTION_REGISTRY = new ActionRegistry({items:[]})

export function Layout() {

  // Stores all known actions in a registry
  const [actions, setActions] = useState<ActionRegistry>(EMPTY_ACTION_REGISTRY);

  // Displayed errors
  const [error, setError] = useState<Problem | null>(null);

  // Navigation tools
  const navigate = useNavigate();
  const matchRoute = useMatchRoute();
  const { pathname } = useLocation();

  // Used to filter out navigation items if user is not technical
  const { isDetailLevelTech } = useDetailLevelContext();

  // Translations
  const { t } = useAppI18n();

  // Tooling for action managers so they can provide context and adapt their
  // behaviours when action succeed (post actions)
  const actionPostHooks = useMemo(
    () => new ActionPostHooks([modelActionPostHook, tagActionPostHook]),
    [],
  );

  // Authentication needed to reload actions when user token is refreshed or
  // created, so the action list matches current user permissions.
  const authentication = useAuthentication();
  const hasLoadedAuthenticatedActionsRef = useRef(false);

  // Load actions from the backend. Be careful, they are filtered depending
  // on user permissions.
  const loadActions = () => {
    fetchActionDescriptors()
      .then((dto) => {
        setActions(new ActionRegistry(dto));
        setError(null);
      })
      .catch((err) => {
        setError(toProblem(err));
      });
  };

  // Immediate loading (public actions or if token is already ready)
  useEffect(() => {
    loadActions();
  }, []);

  // Reload each time authentication is ready with a session token
  // because actions may change depending on user permissions
  useEffect(() => {
    if (authentication.isLoading) return;
    if (!authentication.isAuthenticated) return;
    if (hasLoadedAuthenticatedActionsRef.current) return;

    hasLoadedAuthenticatedActionsRef.current = true;
    loadActions();
  }, [authentication.isLoading, authentication.isAuthenticated]);


  const matchPath = (path: string | undefined) =>
    !!matchRoute({ to: path, fuzzy: true });

  const navigationItemsBase: NavigationTreeItem[] = [
    {
      id: "home",
      parentId: null,
      type: "page",
      path: "/",
      label: t("layout_homeLabel"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "models",
      parentId: null,
      type: "page",
      path: "/models",
      label: t("layout_modelsLabel"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "model-compare",
      parentId: null,
      type: "page",
      path: "/model-compare",
      label: t("layout_modelCompareLabel"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "commands",
      parentId: null,
      type: "page",
      path: "/commands",
      label: t("layout_commandsLabel"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "reports",
      parentId: null,
      type: "page",
      path: "/reports",
      label: t("layout_reportsLabel"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "tag-groups",
      parentId: null,
      type: "page",
      path: "/tag-groups",
      label: t("layout_tagGroupsLabel"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "preferences",
      parentId: null,
      type: "page",
      path: "/preferences",
      label: t("layout_preferencesLabel"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
  ];
  const nav = navigationItemsBase.filter(
    (it) => it.id !== "commands" || isDetailLevelTech,
  );

  return (
    <>
      <UnauthorizedHandler />
      <ApplicationShellSecured
        applicationName={"Medatarun"}
        applicationIcon={
          <img
            src={logo}
            alt="Medatarun logo"
            style={{
              width: "2em",
              height: "2em",
            }}
          />
        }
        pathname={pathname}
        outlet={
          <>
            {actions.isNotEmpty() && (
              <ActionsContext value={actions}>
                <ActionProvider postHooks={actionPostHooks}>
                  <ErrorBoundary>
                    <Outlet />
                    <ActionPerformerView />
                  </ErrorBoundary>
                </ActionProvider>
              </ActionsContext>
            )}
            {actions.isEmpty() && <Loader loading={true} />}
            {error && (
              <div>
                <p>{t("layout_loadingErrorMessage")}</p>
                <ErrorBox error={error} />
              </div>
            )}
          </>
        }
        navigate={(path) => navigate({ to: path })}
        onClickHome={() => navigate({ to: "/" })}
        navigationItems={nav}
        matchPath={matchPath}
      ></ApplicationShellSecured>
    </>
  );
}
