/**
 * Relationship definition without all attributes
 */
export interface RelationshipDefSummaryDto {
  id: string
  name: string | null
  description: string | null
  roles: RelationshipRoleDefDto[]
}

export interface RelationshipRoleDefDto {
  id: string
  name: string | null
  entityId: string
  cardinality: string
}