import { useCurrentActor } from "@/components/business/auth-actor";
import { MessageBar } from "@fluentui/react-components";

export function AdminDangerPanel() {
  const actor = useCurrentActor();
  const isAdmin = actor.isAdmin();
  if (!isAdmin) return null;
  return (
    <MessageBar intent={"warning"}>
      You are using an administrator account. Be careful as it may be dangerous.
    </MessageBar>
  );
}
