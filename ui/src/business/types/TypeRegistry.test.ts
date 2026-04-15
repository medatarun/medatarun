import { describe, expect, test } from "vitest";
import { createRegistry, TypeRegistry } from "@/business/types/TypeRegistry.ts";
import { inspect_type_system_static } from "@/business/types/inspect_type_system.static.ts";

describe("TypeRegistry", () => {
  test("that registry builds", () => {
    const r: TypeRegistry = createRegistry();
    const entityKey = r.findTypeByIdOptional("Username");
    expect(entityKey).not.toBeNullable();
  });

  test("that normalize works", () => {
    const r: TypeRegistry = createRegistry();
    const options = { optional: true };
    expect(r.normalize("Username", options, "hello")).toBe("hello");
    expect(r.normalize("Username", options, "")).toBeNull();
  });
  test("that registry backend types have TypeDeclaration equivalent", () => {
    const r: TypeRegistry = createRegistry();
    for (const item of inspect_type_system_static.items) {
      expect(
        r.findTypeByIdOptional(item.id),
        `type [${item.id}]`,
      ).toBeDefined();
    }
  });
  test("that registry all TypeDeclaration have a backend type equivalent", () => {
    const r: TypeRegistry = createRegistry();
    for (const item of inspect_type_system_static.items) {
      expect(r.findDtoByIdOptional(item.id), `type [${item.id}]`).toBeDefined();
    }
  });
  test("that normalize optional=true boolean works", () => {
    const r: TypeRegistry = createRegistry();
    const opt = { optional: true };
    const test = (value: unknown, expected: boolean | null) =>
      expect(
        r.normalize("Boolean", opt, value),
        "optional=true value=" + value + " as " + typeof value,
      ).toStrictEqual(expected);
    test(true, true);
    test(false, false);
    test("true", true);
    test("false", false);
    test("", false);
    test("niania", false);
    test(null, null);
    test(undefined, null);
  });
  test("that normalize optional=false boolean works", () => {
    const r: TypeRegistry = createRegistry();
    const opt = { optional: false };
    const test = (value: unknown, expected: boolean | null) =>
      expect(
        r.normalize("Boolean", opt, value),
        "optional=false value=" + value + " as " + typeof value,
      ).toStrictEqual(expected);
    test(true, true);
    test(false, false);
    test("true", true);
    test("false", false);
    test("", false);
    test("niania", false);
    test(null, false);
    test(undefined, false);
  });
  test("that normalize List<string> works", () => {
    const r: TypeRegistry = createRegistry();
    expect(
      r.normalize("List<String>", { optional: true }, ["a", "b", "c"]),
    ).toEqual(["a", "b", "c"]);
  });
});
