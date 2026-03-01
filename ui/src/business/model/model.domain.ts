import type {ModelDto, TypeDto} from "./model.dto.ts";


export class Model {
  public dto: ModelDto;

  constructor(dto: ModelDto) {
    this.dto = dto
  }

  findEntityNameOrKey(id: string): string | null {
    const e = this.dto.entities.find(it => it.id === id)
    return e?.name ?? e?.key ?? null
  }

  findEntityKey(id: string): string | null {
    const e = this.dto.entities.find(it => it.id === id)
    return e?.key ?? null
  }

  get nameOrKey() {
    return this.dto.name ?? this.dto.key
  }

  get id() {
    return this.dto.id
  }

  findTypeNameOrKey(typeId: string) {
    const type = this.dto.types.find(it => it.id === typeId);
    return type?.name ?? type?.key ?? type?.id
  }

  findTypeKey(typeId: string) {
    const type = this.dto.types.find(it => it.id === typeId);
    return type?.key
  }

  findEntityDto(entityId: string) {
    return this.dto.entities.find(it => it.id === entityId)

  }

  findEntityAttributeDto(entityId: string, attributeId: string) {
    return this.dto.entities.find(it => it.id === entityId)
      ?.attributes?.find(it => it.id === attributeId)
  }

  findRelationshipDto(relationshipId: string) {
    return this.dto.relationships.find(it => it.id === relationshipId)
  }

  findRelationshipNameOrKey(id: string): string | null {
    const e = this.dto.relationships.find(it => it.id === id)
    return e?.name ?? e?.key ?? null
  }


  findRelationshipAttributeDto(relationshipId: string, attributeId: string) {
    return this.dto.relationships.find(it => it.id === relationshipId)
      ?.attributes?.find(it => it.id === attributeId)
  }

  findTypeDto(typeId: string): TypeDto | undefined {
    return this.dto.types.find(it => it.id === typeId)
  }
}