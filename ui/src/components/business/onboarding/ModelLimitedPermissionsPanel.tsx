import { useCurrentActor } from "@/components/business/auth-actor";
import { useSecurityContext } from "@/components/business/security";
import { useAppI18n } from "@/services/appI18n.tsx";
import { Link } from "@tanstack/react-router";
import { MessageBox } from "@/components/core/MessageBox.tsx";
import type { PropsWithStyle } from "@/components/core";

export function ModelLimitedPermissionsPanel(props: PropsWithStyle) {
  const actor = useCurrentActor();
  const sec = useSecurityContext();
  const { t } = useAppI18n();
  const isAdmin = actor.isAdmin();
  const cannotRead = !sec.canExecuteAction("model_list");
  if (!cannotRead) return null;

  return (
    <MessageBox {...props} intent={"info"}>
      <div>
        <strong>{t("modelLimitedPermissionsPanel_title")}</strong>
      </div>
      <div>
        <p>{t("modelLimitedPermissionsPanel_descriptionModels")}</p>
        {isAdmin && (
          <div>
            <strong>{t("modelLimitedPermissionsPanel_adminIntro")} </strong>
            <ul>
              <li>
                <Link to={"/admin/users"}>
                  {t("modelLimitedPermissionsPanel_adminCreateUser")}
                </Link>
              </li>
              <li>
                <Link to={"/admin/actors"}>
                  {t("modelLimitedPermissionsPanel_adminAssignRole")}
                </Link>
              </li>
              <li>
                <Link to={"/admin/roles"}>
                  {t("modelLimitedPermissionsPanel_adminCreateRole")}
                </Link>
              </li>
            </ul>
          </div>
        )}
        {!isAdmin && <p>{t("modelLimitedPermissionsPanel_nonAdminHelp")}</p>}
      </div>
    </MessageBox>
  );
}
