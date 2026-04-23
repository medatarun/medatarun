import { api } from "@/services/api.ts";
import { Problem } from "@seij/common-types";
import { notifyUnauthorized } from "@/services/user-session-expired.ts";
import type { ActionPayload, ActionResp } from "./action-types.ts";

/**
 * Executes an action, the result can be text or Json or something else.
 *
 * Uniformize error handling and session expiration, injects the session token.
 *
 * Note that this is used both for query and mutations as everything
 * is architectured with the action protocol.
 *
 * Returns what the server returns in an encapsulated object with the type. Json is decoded if needed.
 *
 * This is a quite raw function, don't use it publicly
 *
 * @param actionGroup group of action
 * @param actionName key of the action
 * @param payload payload to send
 */
export async function executeActionInternal<T = unknown>(
  actionGroup: string,
  actionName: string,
  payload: ActionPayload,
): Promise<ActionResp<T>> {
  const headers = api().createHeaders();
  return fetch("/api/" + actionGroup + "/" + actionName, {
    method: "POST",
    headers: headers,
    body: JSON.stringify(payload),
  })
    .then(async (res) => {
      const isError = res.status >= 400;
      if (isError) {
        if (res.status === 401) {
          notifyUnauthorized();
        }
        const errorPayload = await res.json();
        throw new Problem(errorPayload);
      } else {
        // Json response
        const type = res.headers.get("content-type") || "";
        if (type.includes("application/json")) {
          const actionRespJson: ActionResp<T> = {
            contentType: "json",
            json: (await res.json()) as unknown as T,
          };
          return actionRespJson;
        }
        // Text or other response
        const t = await res.text();
        const actionRespText: ActionResp<T> = { contentType: "text", text: t };
        return actionRespText;
      }
    })
    .catch((err) => {
      return Promise.reject(err);
    });
}

/**
 * Executes an action and enforces a JSON response contract.
 * Throws when the endpoint responds with a non-JSON content type.
 */
export async function executeActionJsonInternal<T = unknown>(
  actionGroup: string,
  actionName: string,
  payload: ActionPayload,
): Promise<T> {
  const response = await executeActionInternal<T>(
    actionGroup,
    actionName,
    payload,
  );
  if (response.contentType !== "json") {
    throw Error("Expected JSON response for " + actionGroup + "/" + actionName);
  }
  return response.json;
}
