import { Problem } from "@seij/common-types";

type UnauthorizedListener = () => void;

const listeners = new Set<UnauthorizedListener>();
let unauthorizedTriggered = false;

export function subscribeUnauthorized(
  listener: UnauthorizedListener,
): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function notifyUnauthorized() {
  if (unauthorizedTriggered) {
    return;
  }
  unauthorizedTriggered = true;
  listeners.forEach((listener) => listener());
}

export function resetUnauthorized() {
  unauthorizedTriggered = false;
}

export function reportUnauthorizedError(error: unknown) {
  if (isUnauthorizedError(error)) {
    notifyUnauthorized();
  }
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
