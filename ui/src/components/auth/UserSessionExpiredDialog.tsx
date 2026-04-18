import {
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
} from "@fluentui/react-components";
import { useEffect, useState } from "react";
import { useAuthentication } from "@seij/common-ui-auth";
import { Button } from "@seij/common-ui";
import { queryClient } from "@/services/queryClient.ts";
import {
  resetUserSessionExpiredFlag,
  subscribeUserSessionExpired,
} from "@/services/user-session-expired.ts";
import { useAppI18n } from "@/services/appI18n.tsx";

/**
 * Dialog that is triggered when the user session expired
 */
export function UserSessionExpiredDialog() {
  const authentication = useAuthentication();
  const [isDialogOpen, setDialogOpen] = useState(false);
  const { t } = useAppI18n();

  useEffect(() => {
    return subscribeUserSessionExpired(() => {
      queryClient.clear();
      setDialogOpen(true);
    });
  }, [authentication]);

  const handleReconnect = () => {
    authentication.signOut();
    resetUserSessionExpiredFlag();
    setDialogOpen(false);
    authentication.signIn();
  };

  return (
    <Dialog open={isDialogOpen} modalType="alert">
      <DialogSurface>
        <DialogBody>
          <DialogTitle>{t("sessionExpired")}</DialogTitle>
          <DialogContent>{t("sessionExpiredPleaseReconnect")}</DialogContent>
          <DialogActions>
            <Button variant="primary" onClick={handleReconnect}>
              {t("sessionExpiredReconnectButton")}
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  );
}
