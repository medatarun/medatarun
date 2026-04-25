import { useCurrentActor } from "@/components/business/auth-actor";
import { useAppI18n } from "@/services/appI18n.tsx";
import { MessageBox } from "@/components/core/MessageBox.tsx";
import { allStyles, type PropsWithStyle } from "@/components/core";

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
