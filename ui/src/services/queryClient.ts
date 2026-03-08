import { QueryClient } from "@tanstack/react-query";
import { Problem } from "@seij/common-types";

/**
 * Global React Query client for the UI.
 *
 * Why keep a global 404 retry policy while post-action hooks already exist:
 * - post-action hooks handle the nominal path after an action succeeds
 *   (targeted invalidation + business navigation).
 * - this retry policy is a safety net for all other query flows
 *   (deep links, races, stale tabs, screens not covered by post-action hooks).
 *
 * Rule:
 * - never retry 404, because the resource is gone and extra retries only delay UI.
 * - retry transient errors up to 3 attempts.
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        if (isNotFoundError(error)) return false;
        return failureCount < 3;
      },
    },
  },
});

/**
 * Why this helper is defensive instead of checking only `error instanceof Problem`:
 * - `executeAction` usually throws `Problem` for HTTP errors.
 * - but fetch/network failures and JSON parsing failures can throw non-Problem errors.
 * - React Query `retry` receives whatever was thrown by the query function.
 *
 * We therefore detect 404 in both common shapes:
 * 1) canonical `Problem` object (`error.status`)
 * 2) plain object carrying a `status` field.
 *
 * If API error normalization is made strict in one place (always throw Problem),
 * this helper can be simplified to the `Problem` branch only.
 */
function isNotFoundError(error: unknown): boolean {
  if (error instanceof Problem) {
    return error.status === 404;
  }
  if (typeof error !== "object" || error === null) return false;
  if (!("status" in error)) return false;
  return error.status === 404;
}
