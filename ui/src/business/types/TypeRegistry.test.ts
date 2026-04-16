import { describe, expect, test } from "vitest";
import { createRegistry, TypeRegistry } from "@/business/types/TypeRegistry.ts";
import { inspect_type_system_static } from "@/business/types/inspect_type_system.static.ts";
import { valid } from "@seij/common-validation";

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
  describe("that normalize list with whatever optional", () => {
    for (const optional of [true, false]) {
      test(`with ${optional}`, () => {
        const r: TypeRegistry = createRegistry();
        const opt = { optional: optional };
        const test = (value: unknown, expected: string[] | null | undefined) =>
          expect(
            r.normalize("List<String>", opt, value),
            `optional=false value=${value} as ${typeof value}`,
          ).toEqual(expected);
        test(null, []);
        test(undefined, []);
        test(["a"], ["a"]);
        test(["a", "b"], ["a", "b"]);
        test(["a", null, "b"], ["a", "b"]);
        test(["a", undefined, "b"], ["a", "b"]);
      });
    }
  });
  describe("that validate list with whatever optional", () => {
    for (const param of [{ optional: true }, { optional: false }]) {
      test(`with ${param.optional}`, () => {
        const r: TypeRegistry = createRegistry();
        expect(r.validate("List<String>", param, [])).toEqual(valid);
        expect(r.validate("List<String>", param, null)).toEqual(valid);
        expect(r.validate("List<String>", param, undefined)).toEqual(valid);
        expect(r.validate("List<String>", param, ["a"])).toEqual(valid);
        expect(r.validate("List<String>", param, ["a", "b"])).toEqual(valid);
      });
    }
  });
});
