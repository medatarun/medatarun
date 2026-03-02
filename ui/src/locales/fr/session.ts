import { type SessionMessages } from "../contracts/SessionMessages";

export const sessionMessages = {
  sessionExpired: "Session expirée",
  sessionExpiredPleaseReconnect:
    "Votre session a expiré. Reconnectez-vous pour reprendre l'utilisation de l'application.",
  sessionExpiredReconnectButton: "Se reconnecter",
} satisfies SessionMessages;
