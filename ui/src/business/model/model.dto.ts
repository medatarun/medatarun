export interface ModelSummaryDto {
  id: string;
  key: string;
  name: string | null;
  description: string | null;
  authority: "system" | "canonical";
  error: string | null;
  countTypes: number;
  countEntities: number;
  countRelationships: number;
}

export interface ModelDto {
  id: string;
  key: string;
  name: string | null;
  authority: "system" | "canonical";
  version: string;
  documentationHome: string | null;
  tags: string[];
  description: string | null;
  origin: ElementOrigin;
  entities: EntityDto[];
  relationships: RelationshipDto[];
  types: TypeDto[];
}

export interface TypeDto {
  id: string;
  key: string;
  name: string | null;
  description: string | null;
}

export interface ElementOrigin {
  type: "manual" | "uri";
  uri: string | null;
}

export interface EntityDto {
  id: string;
  key: string;
  name: string | null;
  description: string | null;
  documentationHome: string | null;
  tags: string[];
  origin: EntityOriginDto;
  attributes: AttributeDto[];
}

export interface AttributeDto {
  id: string;
  key: string;
  name: string | null;
  description: string | null;
  type: string;
  optional: boolean;
  identifierAttribute: boolean;
  tags: string[];
}

interface EntityOriginDto {
  type: "manual" | "uri";
  uri: string | null;
}

/**
 * Relationship definition without all attributes
 */
export interface RelationshipDto {
  id: string;
  key: string;
  name: string | null;
  description: string | null;
  tags: string[];
  roles: RelationshipRoleDto[];
  attributes: AttributeDto[];
}

export interface RelationshipRoleDto {
  id: string;
  key: string;
  name: string | null;
  entityId: string;
  cardinality: "zeroOrOne" | "many" | "one" | "unknown";
}

export interface SearchResultLocation {
  objectType: string;
  modelId: string;
  modelKey: string;
  modelLabel: string;
  entityId: string | undefined;
  entityKey: string | undefined;
  entityLabel: string | undefined;
  entityAttributeId: string | undefined;
  entityAttributeLabel: string | undefined;
  relationshipId: string | undefined;
  relationshipLabel: string | undefined;
  relationshipAttributeId: string | undefined;
  relationshipAttributeLabel: string | undefined;
}

export interface SearchResult {
  id: string;
  location: SearchResultLocation;
  tags: string[];
}

export interface SearchResults {
  items: SearchResult[];
}

export interface ModelCompareSideDto {
  modelId: string;
  modelKey: string;
  modelVersion: string;
  modelAuthority: "system" | "canonical" | string;
}

export interface ModelCompareEntryDto {
  status: "ADDED" | "DELETED" | "MODIFIED" | string;
  objectType: string;
  modelKey: string;
  typeKey: string | null;
  entityKey: string | null;
  relationshipKey: string | null;
  roleKey: string | null;
  attributeKey: string | null;
  left: Record<string, unknown> | null;
  right: Record<string, unknown> | null;
}

export interface ModelCompareDto {
  scopeApplied: "structural" | "complete" | string;
  left: ModelCompareSideDto;
  right: ModelCompareSideDto;
  entries: ModelCompareEntryDto[];
}

export interface ModelChangeEventDto {
  eventId: string;
  eventType: string;
  eventVersion: number;
  eventSequenceNumber: number;
  createdAt: number;
  modelVersion: string | null;
  actorId: string;
  actorDisplayName: string;
  payload: Record<string, unknown>;
}
export interface ModelChangeEventWithVersionDto extends ModelChangeEventDto {
  modelVersion: string;
}

export interface ModelChangeEventListDto {
  items: ModelChangeEventDto[];
}

export interface ModelChangeEventListWithVersionDto {
  items: ModelChangeEventWithVersionDto[];
}
