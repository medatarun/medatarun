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