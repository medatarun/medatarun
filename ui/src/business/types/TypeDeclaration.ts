import type { ValidationResult } from "@seij/common-validation";

export type NormalizeFn<T> = (
  ctx: TypeDeclarationNormalizeCtx,
  value: unknown,
) => T | null;

export type ValidateFn = (
  ctx: TypeDeclarationValidateCtx,
  value: unknown,
) => ValidationResult;

export interface TypeDeclarationValidateCtx {
  optional: boolean;
}
export interface TypeDeclarationNormalizeCtx {
  optional: boolean;
}
export interface TypeDeclaration<T> {
  id: string;
  validate: ValidateFn;
  /**
   * Called before sending data to backend
   * @param ctx contains context like if in this context data is optional
   */
  normalize: NormalizeFn<T>;
}
