- pbkeys in frictionless converter
- pbkeys in JSON import
- pbkeys in JSON export
- pbkeys in model import
- pbkeys in database import
- change storage events

## models-storage-json

- Upgrade JSON version to 3.0, 
- add "primaryKey" to entity with list of ordered attributes
- add "businessKeys" to attributes { id: string, entityId: string, attributeIds: string[], name: string?, description:string? } 
- remove "identifierAttribute" from entity, 
- add tests for pbkeys

## Documentation

Documentation on database imports pk
Documentation on tableschema imports pk

## UI

- in Model history: entity_identifier_attribute_updated
- in UI.kt manage the keys
- in UI display attributes with the key if they are in the entity primary key

## Copy
- faire des tests sur PK et BK copy

## History
- faire des tests sur PK et BK copy a l'écran
- voir les changements bk et pk

## Diff
- gestion des keys pk et bk
- isEntityStructuralChanged doit dire si les pk ont changé

## Model validation
- [x] suppression du check specifique
- [ ] revoir les TU
- [ ] au niveau TU ajouter des controles sur les relations pk/bk vers entityId, attributeId pour voir si on pointe bien sur des références valides