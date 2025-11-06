# Medatarun Frictionless extension

Goal: be able to read frictionless schemas and derivatives to import them as Medatarun models, 
so that humans, AIs and other tools can understand them. 

Many public offices publish some schema (for example in EU) but understanding them is 
hard because they do not respect the Frictionless Data specs. You get some `schema.json` file
but all layers of _frictionless_ specs are merged into something composite. So you never know
if you are reading a Data Package, a Table Schema or something else. 

Moreover, reading schemas is hard, and the bunch of tools from Frictionless is not consistent. 
Learning curve is hard. 

## Examples

Data schema for French administration, provides a lot of links and the content of https://schema.data.gouv.fr/

- https://github.com/datagouv/schema.data.gouv.fr/blob/main/repertoires.yml

## Choices and questions

- If a file contains TableSchema and DataPackage items (a `fields` property is present in the file),
  the file will be considered as a composite.
- If there is no primary key in TableSchema, we use the first found field as an entity identifier
- Schemas don't always provide a name suitable for entities
  - if the file is a mix with datapackage + tableschema, we try to use informations on the datapackage
  - if the file is a real datapackage, we use informations on the `resources` list or the datapackage itself
    to find names. 
  - as a lot of fields are not required, and files themselves are not always validated, we have to implement
    fallbacks that may make the model unstable.
- Files can be accessed on disk. We don't know if it's a good idea or not to keep it
- All types from the Frictionless specs are imported as is, as Frictionless doesn't declare types and are built-ins.
- Relationships are not available in the specs, so you end up with no real relationships
- Primary keys can be composites, but this is not supported in Medatarun. You may end-up with an entity identifier
  that doesn't match any attributes in the entity (composite primary key items are joined as a single string).







