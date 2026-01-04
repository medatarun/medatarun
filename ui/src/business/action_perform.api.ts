import {api} from "../services/api.ts";

export type ActionResp =
  | { contentType: "text", text: string }
  | { contentType: "json", json: unknown }

export async function executeAction(actionGroup: string, actionName: string, payload: unknown): Promise<ActionResp> {
  const headers = api().createHeaders()
  return fetch("/api/" + actionGroup + "/" + actionName, {
    method: "POST",
    headers: headers,
    body: JSON.stringify(payload)
  })
    .then(async res => {
      const isError = res.status >= 400
      if (isError) {
        throw Error(await res.text())
      } else {
        // Json response
        const type = res.headers.get("content-type") || "";
        if (type.includes("application/json")) {
          const actionRespJson: ActionResp = {contentType: "json", json: await res.json() as unknown};
          return actionRespJson
        }
        // Text or other response
        const t = await res.text();
        const actionRespText: ActionResp = {contentType: "text", text: t};
        return actionRespText
      }
    })
    .catch(err => {
      return Promise.reject(err);
    })
}