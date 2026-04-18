import { Problem } from "@seij/common-types";

/**
 * A listener for Unauthorized http events, triggered when a user is not
 * signed in anymore, mostly because its session expired.
 */
type UnauthorizedListener = () => void;

/**
 * List of listeners
 */
const listeners = new Set<UnauthorizedListener>();

/**
 * Global flag that tells if the signal had been received
 */
let unauthorizedTriggered = false;

/**
 * Registers a listener to a user session expired event
 * @param listener will be called back when we detect user session expired and that he needs to sign in again
 */
export function subscribeUserSessionExpired(
  listener: UnauthorizedListener,
): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

/**
 * Notification sent by the API layer when a call with a token has been done
 * on API and that the API responded with status code UNAUTHORIZED.
 *
 * For us, it means that the session token expired, so we notify listeners.
 */
export function notifyUnauthorized() {
  if (unauthorizedTriggered) {
    return;
  }
  unauthorizedTriggered = true;
  listeners.forEach((listener) => listener());
}
/**
 * Called by API layer when API got a generic error and is not sure whether
 * it is an unauthorized error, because error can have various forms (from
 * the browser, network, or backend in ProblemJson)
 * @param error
 */
export function notifyMaybeUnauthorized(error: unknown) {
  if (isUnauthorizedError(error)) {
    notifyUnauthorized();
  }
}

/**
 * Used by handlers to tell "ok, the user has seen the message", this had been
 * taken into account.
 */
export function resetUserSessionExpiredFlag() {
  unauthorizedTriggered = false;
}

function isUnauthorizedError(error: unknown): boolean {
  if (error instanceof Problem) {
    return error.status === 401;
  }
  if (typeof error !== "object" || error === null) {
    return false;
  }
  return "status" in error && error.status === 401;
}
