# Search Fixture model description

## Tags

- ui-result: local to "crm", indicates a result displayed on screen
- ui-search: local to "crm", indicates a search criterion
- imported: local to "cooking", indicates data imported from another system
- gdpr/personal-data: global, GDPR personal data
- gdpr/special-category-data: global, GDPR special category data (art. 9: health, religion, political opinions, biometrics, etc.)
- security/public: intentionally public data outside the company
- security/internal: internal company data, not public
- security/confidential: data with limited or hidden visibility (passwords, PINs, etc.)

# Models

| Type     | key         | description                               | tags                                             |
|----------|-------------|-------------------------------------------|--------------------------------------------------|
| Model    | crm         |                                           |                                                  |
| + Entity | person      |                                           | ui-result                                        |
| + + Attr | name        |                                           | gdpr/personal-data security/internal ui-search   |
| + + Attr | email       |                                           | gdpr/personal-data security/internal ui-search   |
| + + Attr | password    |                                           | security/confidential                            |
| + Entity | company     |                                           | ui-result                                        |
| + + Attr | name        |                                           | security/public ui-search                        |
| + + Attr | website     |                                           | security/public ui-search ui-result              |
| + Rel    | employment  | person and company                        | ui-result                                        |
| + + Attr | since       |                                           | security/internal ui-result                      |
| Model    | cooking     |                                           | imported                                         |
| + Entity | ingredient  |                                           | imported                                         |
| + + Attr | name        | displayed on website                      | security/public imported                         |
| + + Attr | code        | relationship to our suppliers, not public | security/internal imported                       |
| + Entity | recipe      |                                           | imported                                         |
| + + Attr | name        | displayed on website                      | security/public imported                         |
| + + Attr | description | displayed on website                      | security/public imported                         |
| + Entity | chef        |                                           | imported                                         |
| + + Attr | name        |                                           | security/internal                                |
| + + Attr | email       | company email, not private                | security/internal                                |
| + + Attr | fingerprint |                                           | gdpr/special-category-data security/confidential |
| + Rel    | usage       | ingredient and recipe                     | imported                                         |
| + + Attr | quantity    | displayed on website                      | security/public imported                         |
| + + Attr | unit        | displayed on website                      | security/public imported                         |
| + Rel    | author      | chef and recipe                           | imported                                         |
| + + Attr | date        | displayed on website                      | security/public imported                         |
