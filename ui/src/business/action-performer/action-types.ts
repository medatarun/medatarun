export type ActionResp<T = unknown> =
  | { contentType: "text"; text: string }
  | { contentType: "json"; json: T };

export type ActionPayload = Record<string, unknown>;
