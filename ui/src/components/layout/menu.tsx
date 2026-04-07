import type { NavigationTreeItem } from "@seij/common-ui";
import { useAppI18n } from "@/services/appI18n.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { useCurrentActor } from "@/business/actor";

export function useMenu() {
  const { t } = useAppI18n();

  // Used to filter out navigation items if user is not technical
  const { isDetailLevelTech } = useDetailLevelContext();
  const currentActor = useCurrentActor();
  const navigationItemsBase: NavigationTreeItem[] = [
    /*
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
    */
    {
      id: "models",
      parentId: null,
      type: "page",
      /* path: "/models", */
      path: "/",
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
      id: "integration",
      parentId: null,
      type: "group",
      label: t("menu_integration"),
    },
    {
      id: "commands",
      parentId: "integration",
      type: "page",
      path: "/commands",
      label: t("menu_integration_actions"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "admin",
      parentId: null,
      type: "group",
      label: t("menu_admin"),
    },
    {
      id: "admin_auth_users",
      parentId: "admin",
      type: "page",
      path: "/admin/users",
      label: t("menu_admin_authUsers"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "admin_auth_actors",
      parentId: "admin",
      type: "page",
      path: "/admin/actors",
      label: t("menu_admin_authActors"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "admin_auth_roles",
      parentId: "admin",
      type: "page",
      path: "/admin/roles",
      label: t("menu_admin_authRoles"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },

    {
      id: "admin_db_drivers",
      parentId: "admin",
      type: "page",
      path: "/admin/db-drivers",
      label: t("menu_admin_databaseDrivers"),
      description: undefined,
      icon: "dashboard",
      rule: undefined,
    },
    {
      id: "admin_db_datasources",
      parentId: "admin",
      type: "page",
      path: "/admin/db-datasources",
      label: t("menu_admin_datasources"),
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
      icon: "user",
      rule: undefined,
    },
  ];
  const nav = navigationItemsBase
    .filter((it) => it.id !== "integration" || isDetailLevelTech)
    .filter((it) => it.id !== "commands" || isDetailLevelTech)
    .filter((it) => it.id !== "admin" || currentActor.isAdmin());

  return nav;
}
