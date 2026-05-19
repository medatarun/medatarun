import { useCurrentActor } from "@medatarun/ui/components/business/auth-actor";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import { MessageBox } from "@medatarun/ui/components/core/MessageBox.tsx";
import { allStyles, type PropsWithStyle } from "@medatarun/ui/components/core";

export function AdminDangerPanel(props: PropsWithStyle) {
  const actor = useCurrentActor();
  const { t } = useAppI18n();
  const isAdmin = actor.isAdmin();
  if (!isAdmin) return null;
  return (
    <MessageBox {...allStyles(props)} intent={"warning"}>
      {t("adminDangerPanel_warning")}
    </MessageBox>
  );
}
