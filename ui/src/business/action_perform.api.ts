import {api} from "../services/api.ts";
import {queryClient} from "../services/queryClient.ts";
import {Problem} from "@seij/common-types";

export type ActionResp<T = unknown> =
  | { contentType: "text", text: string }
  | { contentType: "json", json: T }

export type ActionPayload = Record<string, unknown>

export async function executeAction<T = unknown>(actionGroup: string, actionName: string, payload: ActionPayload): Promise<ActionResp<T>> {
  const headers = api().createHeaders()
  return fetch("/api/" + actionGroup + "/" + actionName, {
    method: "POST",
    headers: headers,
    body: JSON.stringify(payload)
  })
    .then(async res => {
      const isError = res.status >= 400
      if (isError) {
        const errorPayload = await res.json()
        throw new Problem(errorPayload)
      } else {
        // Json response
        const type = res.headers.get("content-type") || "";
        if (type.includes("application/json")) {
          const actionRespJson: ActionResp<T> = {contentType: "json", json: await res.json() as unknown as T};
          return actionRespJson
        }
        // Text or other response
        const t = await res.text();
        const actionRespText: ActionResp<T> = {contentType: "text", text: t};
        return actionRespText
      }
    })
    .catch(err => {
      return Promise.reject(err);
    })
}

export function useExecuteAction() {
  const s = executeAction
  return {
    executeAction: async (actionGroupKey: string, actionKey: string, payload: ActionPayload) => {
      const resp = await s(actionGroupKey, actionKey, payload)
      await queryClient.invalidateQueries()
      return resp
    }
  }
}