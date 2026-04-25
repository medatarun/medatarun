import {
  Outlet,
  useLocation,
  useMatchRoute,
  useNavigate,
} from "@tanstack/react-router";
import { ActionPerformerView } from "@/components/business/actions/ActionPerformerView.tsx";
import logo from "/favicon/favicon.svg?url";
import { ErrorBoundary } from "./ErrorBoundary.tsx";
import { ApplicationShellSecured } from "@seij/common-ui-auth";
import { UserSessionExpiredDialog } from "@/components/auth/UserSessionExpiredDialog.tsx";
import { useMenu } from "./menu.tsx";

export function Layout() {
  // Navigation tools
  const navigate = useNavigate();
  const matchRoute = useMatchRoute();
  const { pathname } = useLocation();

  // Menu
  const menu = useMenu();

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
          <ErrorBoundary>
            <Outlet />
            <ActionPerformerView />
          </ErrorBoundary>
        }
        navigate={(path) => navigate({ to: path })}
        onClickHome={() => navigate({ to: "/" })}
        navigationItems={menu}
        matchPath={matchPath}
      ></ApplicationShellSecured>
    </>
  );
}
