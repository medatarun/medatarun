import type {RelationshipDto} from "./relationships.tsx";


export interface ModelSummaryDto {
  id: string,
  name: string | null,
  description: string | null,
  error: string | null,
  countTypes: number,
  countEntities: number,
  countRelationships: number
}

export interface ModelDto {
  id: string
  name: string | null
  version: string
  documentationHome: string | null
  hashtags: string[]
  description: string | null
  origin: ElementOrigin
  entityDefs: EntityDto[]
  relationshipDefs: RelationshipDto[]
  types: TypeDto[]
}

export interface TypeDto {
  id: string
  name: string | null
  description: string | null
}


export interface ElementOrigin {
  type: "manual" | "uri",
  uri: string | null
}


export interface EntityDto {
  id: string
  name: string | null
  description: string | null
  origin: EntityDefOriginDto
  documentationHome: string | null
  hashtags: string[]
  attributes: AttributeDto[]
  model: {
    id: string
    name: string | null
  }
}

export interface AttributeDto {
  id: string
  type: string
  optional: boolean
  identifierAttribute: boolean
  name: string | null
  description: string | null
  hashtags: string[]
}

interface EntityDefOriginDto {
  type: "manual" | "uri",
  uri: string | null
}


export class Model {
  public dto: ModelDto;
  constructor(dto: ModelDto) {
    this.dto = dto
  }

  findEntityName(id: string): string | null {
    const e = this.dto.entityDefs.find(it => it.id === id)
    return e?.name ?? null
  }

  get nameOrId (){
    return this.dto.name ?? this.dto.id
  }
  get id() { return this.dto.id }

  findTypeName(typeId: string) {
    const type = this.dto.types.find(it => it.id === typeId);
    return type?.name ?? type?.id
  }

  findEntityDto(entityId: string) {
    return this.dto.entityDefs.find(it => it.id === entityId)

  }
  findEntityAttributeDto(entityId: string, attributeId: string) {
    return this.dto.entityDefs.find(it => it.id === entityId)
      ?.attributes?.find(it => it.id === attributeId)
  }
  findRelationshipDto(relationshipId: string) {
    return this.dto.relationshipDefs.find(it => it.id === relationshipId)

  }
  findRelationshipAttributeDto(relationshipId: string, attributeId: string) {
    return this.dto.relationshipDefs.find(it => it.id === relationshipId)
      ?.attributes?.find(it => it.id === attributeId)
  }

  findTypeDto(typeId: string) : TypeDto | undefined{
    return this.dto.types.find(it => it.id === typeId)
  }
}