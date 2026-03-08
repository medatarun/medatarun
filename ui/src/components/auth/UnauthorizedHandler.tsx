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
  resetUnauthorized,
  subscribeUnauthorized,
} from "@/services/unauthorized.ts";
import { useAppI18n } from "@/services/appI18n.tsx";

export function UnauthorizedHandler() {
  const authentication = useAuthentication();
  const [isDialogOpen, setDialogOpen] = useState(false);
  const { t } = useAppI18n();

  useEffect(() => {
    return subscribeUnauthorized(() => {
      queryClient.clear();
      setDialogOpen(true);
    });
  }, [authentication]);

  const handleReconnect = () => {
    resetUnauthorized();
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
