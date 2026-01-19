export const ActionUILocations = {
  entity: "entity",
  entity_attribute: "entity.attribute",
  entity_attributes: "entity.attributes",
  entity_relationships: "entity.relationships",
  global: "global",
  hidden: "hidden",
  model_entities: "model.entities",
  model_overview: "model.overview",
  model_relationships: "model.relationships",
  model_types: "model.types",
  models: "models",
  preferences: "preferences",
  relationship: "relationship",
  relationship_attribute: "relationship.attribute",
  relationship_attributes: "relationship.attributes",
  type: "type",
} as const

export type ActionUILocation = (typeof ActionUILocations)[keyof typeof ActionUILocations]