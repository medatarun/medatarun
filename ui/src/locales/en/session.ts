import { type SessionMessages } from "../contracts/SessionMessages";

export const sessionMessages = {
  sessionExpired: "Session expired",
  sessionExpiredPleaseReconnect:
    "Your session expired. Please reconnect to continue using the application.",
  sessionExpiredReconnectButton: "Sign-in again",
} satisfies SessionMessages;
