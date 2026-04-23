import {
  Outlet,
  useLocation,
  useMatchRoute,
  useNavigate,
} from "@tanstack/react-router";
import { useMemo } from "react";
import {
  ActionRegistry,
  ActionsContext,
  useActionRegistry,
  useActionRegistryQuery,
} from "@/business/action_registry";
import { ActionPerformerView } from "@/components/business/actions/ActionPerformerView.tsx";
import { ActionProvider } from "@/components/business/actions/ActionPerformerProvider.tsx";
import logo from "/favicon/favicon.svg?url";
import { ErrorBoundary } from "./ErrorBoundary.tsx";
import { ErrorBox, Loader } from "@seij/common-ui";
import {
  ApplicationShellSecured,
  useAuthentication,
} from "@seij/common-ui-auth";
import { UserSessionExpiredDialog } from "@/components/auth/UserSessionExpiredDialog.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  ActionPostHookCompat,
  ActionPostHooks,
} from "@/business/action-performer";
import { toProblem } from "@seij/common-types";
import { useMenu } from "./menu.tsx";
import { queryClient } from "@/services/queryClient.ts";

const EMPTY_ACTION_REGISTRY = new ActionRegistry({ items: [] });

export function Layout() {
  // Navigation tools
  const navigate = useNavigate();
  const matchRoute = useMatchRoute();
  const { pathname } = useLocation();

  // Menu
  const menu = useMenu();

  // Translations
  const { t } = useAppI18n();

  // Authentication needed to reload actions when user token is refreshed or
  // created, so the action list matches current user permissions.
  const authentication = useAuthentication();
  const actionAccessScope = authentication.isAuthenticated
    ? "authenticated"
    : "public";

  // Action registry is loaded depending on current authentication state
  // and reloaded when it changes
  const actionsQuery = useActionRegistryQuery(actionAccessScope);
  const actions = actionsQuery.data ?? EMPTY_ACTION_REGISTRY;

  // Tooling for action managers so they can provide context and adapt their
  // behavior when action succeeds (post actions)
  const actionPostHooks = useMemo(
    () => new ActionPostHooks([new ActionPostHookCompat(actions, queryClient)]),
    [actions],
  );

  const error = actionsQuery.error ? toProblem(actionsQuery.error) : null;

  const matchPath = (path: string | undefined) =>
    !!matchRoute({ to: path, fuzzy: true });

  return (
    <>
      <UserSessionExpiredDialog />
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
            {actionsQuery.isLoading && <Loader loading={true} />}
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
        navigationItems={menu}
        matchPath={matchPath}
      ></ApplicationShellSecured>
    </>
  );
}
