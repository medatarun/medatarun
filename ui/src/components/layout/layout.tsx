import {
  Outlet,
  useLocation,
  useMatchRoute,
  useNavigate,
} from "@tanstack/react-router";
import { MessageBar } from "@fluentui/react-components";
import { useEffect, useState } from "react";
import {
  ActionRegistry,
  ActionsContext,
  fetchActionDescriptors,
} from "@/business/action_registry";
import { ActionPerformerView } from "@/components/business/actions/ActionPerformerView.tsx";
import { ActionProvider } from "@/components/business/actions/ActionPerformerProvider.tsx";
import logo from "../../../public/favicon/favicon.svg";
import { ErrorBoundary } from "./ErrorBoundary.tsx";
import { Loader, type NavigationTreeItem } from "@seij/common-ui";
import { ApplicationShellSecured } from "@seij/common-ui-auth";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { UnauthorizedHandler } from "@/components/auth/UnauthorizedHandler.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";

export function Layout2() {
  const [actions, setActions] = useState<ActionRegistry>();
  const [error, setError] = useState<any | null>(null);
  const navigate = useNavigate();
  const matchRoute = useMatchRoute();
  const { pathname } = useLocation();
  const { isDetailLevelTech } = useDetailLevelContext();
  const { t } = useAppI18n();

  useEffect(() => {
    fetchActionDescriptors()
      .then((dto) => {
        const ar = new ActionRegistry(dto);
        setActions(ar);
      })
      .catch((err) => {
        setError(err);
        console.log(err);
      });
  }, []);

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
            {actions && (
              <ActionsContext value={actions}>
                <ActionProvider>
                  <ErrorBoundary>
                    <Outlet />
                    <ActionPerformerView />
                  </ErrorBoundary>
                </ActionProvider>
              </ActionsContext>
            )}
            {!actions && <Loader loading={true} />}
            {error && (
              <div>
                <p>{t("layout_loadingErrorMessage")}</p>
                <MessageBar intent="error">{JSON.stringify(error)}</MessageBar>
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
