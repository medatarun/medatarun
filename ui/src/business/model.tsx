import type {RelationshipDto} from "./relationships.tsx";


export interface ModelSummaryDto {
  id: string,
  key: string,
  name: string | null,
  description: string | null,
  error: string | null,
  countTypes: number,
  countEntities: number,
  countRelationships: number
}

export interface ModelDto {
  id: string
  key: string
  name: string | null
  version: string
  documentationHome: string | null
  hashtags: string[]
  description: string | null
  origin: ElementOrigin
  entities: EntityDto[]
  relationships: RelationshipDto[]
  types: TypeDto[]
}

export interface TypeDto {
  id: string
  key: string
  name: string | null
  description: string | null
}


export interface ElementOrigin {
  type: "manual" | "uri",
  uri: string | null
}


export interface EntityDto {
  id: string
  key: string
  name: string | null
  description: string | null
  documentationHome: string | null
  hashtags: string[]
  model: {
    id: string
    name: string | null
  }
  origin: EntityOriginDto
  attributes: AttributeDto[]
}

export interface AttributeDto {
  id: string
  key: string
  name: string | null
  description: string | null
  type: string
  optional: boolean
  identifierAttribute: boolean
  hashtags: string[]
}

interface EntityOriginDto {
  type: "manual" | "uri",
  uri: string | null
}


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

  findRelationshipAttributeDto(relationshipId: string, attributeId: string) {
    return this.dto.relationships.find(it => it.id === relationshipId)
      ?.attributes?.find(it => it.id === attributeId)
  }

  findTypeDto(typeId: string): TypeDto | undefined {
    return this.dto.types.find(it => it.id === typeId)
  }
}