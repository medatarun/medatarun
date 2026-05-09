import { describe, expect, test } from "vitest";
import { createAction } from "./action-registry-dictionnary.ts";

describe("action registry dictionnary builds", () => {
  test("action created", () => {
    // This is mostly for type checking and see there is no typescript errors
    const action = createAction("auth/actor_enable", { actorId: "abcd" });
    expect(action.action).toBe("auth/actor_enable");
    expect(action.payload.actorId).toBe("abcd");
  });
});
