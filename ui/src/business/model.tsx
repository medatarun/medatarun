import type {RelationshipDefSummaryDto} from "./relationships.tsx";


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
  relationshipDefs: RelationshipDefSummaryDto[]
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
  attributes: EntityAttributeDto[]
  model: {
    id: string
    name: string | null
  }
}

interface EntityAttributeDto {
  id: string
  type: string
  optional: boolean
  identifierAttribute: boolean
  name: string | null
  description: string | null
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
}