import { useCurrentActor } from "@/components/business/auth-actor";
import { useSecurityContext } from "@/components/business/security";
import { useAppI18n } from "@/services/appI18n.tsx";
import { Link } from "@tanstack/react-router";

export function ModelLimitedPermissionsPanel() {
  const actor = useCurrentActor();
  const sec = useSecurityContext();
  const { t } = useAppI18n();
  const isAdmin = actor.isAdmin();
  const cannotRead = !sec.canExecuteAction("model_list");
  if (!cannotRead) return null;
  return (
    <div>
      <p>
        <strong>{t("modelLimitedPermissionsPanel_title")}</strong>
      </p>
      <div>
        <p>{t("modelLimitedPermissionsPanel_descriptionModels")}</p>
        <p>{t("modelLimitedPermissionsPanel_descriptionPermission")}</p>
        <p>{t("modelLimitedPermissionsPanel_descriptionIntent")}</p>
        {isAdmin && (
          <div>
            <strong>{t("modelLimitedPermissionsPanel_adminIntro")} </strong>
            <ul>
              <li>
                <Link to={"/admin/users"}>
                  {t("modelLimitedPermissionsPanel_adminCreateUser")}
                </Link>
              </li>
              <li>{t("modelLimitedPermissionsPanel_adminAssignRole")}</li>
            </ul>
          </div>
        )}
        {!isAdmin && <p>{t("modelLimitedPermissionsPanel_nonAdminHelp")}</p>}
      </div>
    </div>
  );
}
