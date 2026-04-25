import { useCurrentActor } from "@/components/business/auth-actor";
import { useAppI18n } from "@/services/appI18n.tsx";
import { MessageBox } from "@/components/core/MessageBox.tsx";
import { tokens } from "@fluentui/react-components";

export function AdminDangerPanel() {
  const actor = useCurrentActor();
  const { t } = useAppI18n();
  const isAdmin = actor.isAdmin();
  if (!isAdmin) return null;
  return (
    <MessageBox
      intent={"warning"}
      styles={{ marginTop: tokens.spacingVerticalM }}
    >
      {t("adminDangerPanel_warning")}
    </MessageBox>
  );
}
