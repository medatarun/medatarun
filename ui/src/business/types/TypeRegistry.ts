import type { TypeDescriptorDto } from "./TypeDescriptorDto.ts";
import { inspect_type_system_static } from "./inspect_type_system.static.ts";
import { registeredTypes } from "./types.ts";
import type {
  TypeDeclaration,
  TypeDeclarationNormalizeCtx,
  TypeDeclarationValidateCtx,
} from "@/business/types/TypeDeclaration.ts";
import { isArray, isNil } from "lodash-es";
import { throwError } from "@seij/common-types";
import {
  combineValidationResults,
  invalid,
  valid,
  type ValidationResult,
} from "@seij/common-validation";
import { appT } from "@/services/appI18n.tsx";

const t = appT;

export class TypeRegistry {
  private _typeDtoList: TypeDescriptorDto[];
  private _registeredTypes: TypeDeclaration<unknown>[];
  private _typeMap: Map<string, TypeDeclaration<unknown>>;
  private _typeDtoMap: Map<string, TypeDescriptorDto>;

  constructor(
    typeDtoList: TypeDescriptorDto[],
    registeredTypes: TypeDeclaration<unknown>[],
  ) {
    this._typeDtoList = typeDtoList;
    this._registeredTypes = registeredTypes;
    this._typeMap = new Map(registeredTypes.map((it) => [it.id, it]));
    this._typeDtoMap = new Map(typeDtoList.map((it) => [it.id, it]));
  }

  public findTypeByIdOptional(
    id: string,
  ): TypeDeclaration<unknown> | undefined {
    return this._typeMap.get(id);
  }

  public findDtoByIdOptional(id: string): TypeDescriptorDto | undefined {
    return this._typeDtoMap.get(id);
  }

  #findType(id: string): TypeDeclaration<unknown> {
    return this._typeMap.get(id) ?? throwError(`type not found ${id}`);
  }

  #findTypeOptional(id: string): TypeDeclaration<unknown> | undefined {
    return this._typeMap.get(id);
  }

  typeDecode(id: string): { type: string; isList: boolean } {
    if (id.startsWith("List<") && id.endsWith(">")) {
      return { type: id.substring(5, id.length - 1), isList: true };
    }
    return { type: id, isList: false };
  }

  public normalize(
    id: string,
    ctx: TypeDeclarationNormalizeCtx,
    value: unknown,
  ): unknown {
    const typeDecoded = this.typeDecode(id);
    if (typeDecoded.isList) {
      const t = this.#findType(typeDecoded.type);
      return this.normalizeList(ctx, t, value);
    }
    return this.#findType(id).normalize(ctx, value);
  }

  private normalizeList(
    ctx: TypeDeclarationNormalizeCtx,
    typeDeclared: TypeDeclaration<unknown>,
    value: unknown,
  ) {
    if (value === null || value === undefined) {
      return [];
    }
    const normalized = [];
    if (isArray(value)) {
      for (const item of value) {
        if (isNil(item)) continue;
        const itemNormalized = typeDeclared.normalize(ctx, item);
        if (!isNil(itemNormalized)) normalized.push(itemNormalized);
      }
    }
    return normalized;
  }

  validate(
    id: string,
    ctx: TypeDeclarationValidateCtx,
    value: unknown,
  ): ValidationResult {
    const typeDecoded = this.typeDecode(id);
    if (typeDecoded.isList) {
      const t = this.#findTypeOptional(typeDecoded.type);
      if (!t) return this.#validationUknownType(typeDecoded.type);
      return this.#validateList(ctx, t, value);
    }
    return this.#findType(id).validate(ctx, value);
  }

  #validateList(
    ctx: TypeDeclarationNormalizeCtx,
    typeDeclared: TypeDeclaration<unknown>,
    value: unknown,
  ) {
    // List should not depend on optional.
    // Optional means null or not null in actions, not how many items are
    // in the list (0, 1 or more). So if the value is null or undefined or
    // empty, it is the same, and it is valid.
    //
    // When the action is sent to the backend, normalization will transform
    // nulls and undefined into empty arrays anyway.
    // The optional in context is for elements in the list (it if it makes any
    // sense), but actions in general would never say List<X> nullable.
    if (value === undefined || value === null) {
      return valid;
    }
    if (isArray(value)) {
      if (value.length === 0) {
        return valid;
      } else {
        return combineValidationResults(
          value.map((v) => this.validate(typeDeclared.id, ctx, v)),
        );
      }
    }
    return this.#validationUknownType("List<" + typeDeclared.id + ">");
  }

  #validationUknownType(type: string) {
    return invalid(t("formValidation_unsupportedType", { type: type }));
  }
}

export function createRegistry(): TypeRegistry {
  return new TypeRegistry(inspect_type_system_static.items, registeredTypes);
}

export const TypeRegistryInstance: TypeRegistry = createRegistry();
