import { useCurrentActor } from "@/components/business/auth-actor";
import { useAppI18n } from "@/services/appI18n.tsx";
import { MessageBar } from "@fluentui/react-components";

export function AdminDangerPanel() {
  const actor = useCurrentActor();
  const { t } = useAppI18n();
  const isAdmin = actor.isAdmin();
  if (!isAdmin) return null;
  return (
    <MessageBar intent={"warning"}>{t("adminDangerPanel_warning")}</MessageBar>
  );
}
