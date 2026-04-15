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
import { invalid, type ValidationResult } from "@seij/common-validation";
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
