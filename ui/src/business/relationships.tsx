import type {AttributeDto} from "./model.tsx";

/**
 * Relationship definition without all attributes
 */
export interface RelationshipDto {
  id: string
  key: string
  name: string | null
  description: string | null
  hashtags: string[],
  roles: RelationshipRoleDto[],
  attributes: AttributeDto[]
}

export interface RelationshipRoleDto {
  id: string
  key: string
  name: string | null
  entityId: string
  cardinality: "zeroOrOne" | "many" | "one" | "unknown"
}