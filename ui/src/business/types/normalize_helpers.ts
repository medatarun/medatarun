import type { TypeDeclarationNormalizeCtx } from "@/business/types/TypeDeclaration.ts";

export function normalizeString(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): string | null {
  if (value === null && ctx.optional) return null;
  if (value === undefined && ctx.optional) return null;
  if (value === "") return null;
  if (typeof value === "string") return value;
  return "" + value;
}

export function normalizeBoolean(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): boolean | null {
  if (value === null && ctx.optional) return null;
  if (value === undefined && ctx.optional) return null;
  if (typeof value == "boolean") return value;
  return value === "true";
}

export function normalizeKey(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): string | null {
  return normalizeString(ctx, value);
}

export function normalizeRef(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): string | null {
  return normalizeString(ctx, value);
}
export function normalizeNone<T>(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): T | null {
  return value as T;
}
export function normalizeId(
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
): string | null {
  return normalizeString(ctx, value);
}
