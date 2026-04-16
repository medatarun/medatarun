# Test quality rules

- Utiliser uniquement ModelTestEnv (pas TestEnvOneModel, pas env custom partagé
  type TestEnvEntityUpdate).
- Utiliser les helpers ModelTestEnv pour le setup (modelCreate, typeCreate,
  entityCreate2, entityAttributeCreate, etc.) au lieu de dispatcher toutes les
  actions brutes de création **sauf** si le test concerne explicitement cette
  action.
- Utiliser les helpers de refs (entityRefKey("..."), typeRefKey("..."), etc.) et
  réutiliser ref.key au lieu de recréer EntityKey("...")/TypeKey("...") inline.
- Ne plus utiliser Entity_Create mais Entity_Create2
- Quand un test a besoin d’une PK explicite: faire Entity_Create2 puis
  EntityAttribute_Create + EntityPrimaryKey_Update (au lieu de Entity_Create).
- Ajouter au moins un bloc replayWithRebuild { ... } sur le scénario nominal du
  test.
- Pour les tests d’update (Entity_UpdateDescription_Test et similaires), ajouter
  un test no-op “same value” avec findLastStoredModelChangeEvent pour vérifier
  qu’aucun event inutile n’est écrit.
- Garder des données totalement locales à chaque test (aucun partage implicite
  entre tests).
- Sur les helpers ModelTestEnv, éviter les noms d’arguments quand l’ordre est
  évident (env.modelCreate(modelRef.key), env.typeCreate(modelRef,
  typeRef.key)).
- Ne pas utiliser findModelAggregate() sauf pour de rares operations où le
  modèle entier doit être chargé. Privilégier les méthodes de query.xxx pour
  faire des lectures ciblées. Si ces fonctions manquent demander l'autorisation
  de les créer. 
- Arguments nommés :on fait positionnel par défaut pour les appels courts et stables (1-2 args, ou helpers connus en test setup).
- Named arguments obligatoires dès qu’il y a risque d’ambiguïté: même type répété, null, booléens, 3+ paramètres métier, ou action domain sensible.
- Dans ce repo de tests: setup helper ModelTestEnv plutôt positionnel; ModelAction.* plutôt nommé.

## Status (models-core)

Indique si on a déjà tout checké dans fichiers ou s'il reste des choses à faire côté qualité


- [X] [BusinessKey_Create_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/BusinessKey_Create_Test.kt)
- [X] [BusinessKey_UpdateDescription_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/BusinessKey_UpdateDescription_Test.kt)
- [X] [BusinessKey_UpdateKey_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/BusinessKey_UpdateKey_Test.kt)
- [X] [BusinessKey_UpdateName_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/BusinessKey_UpdateName_Test.kt)
- [X] [BusinessKey_UpdateX_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/BusinessKey_UpdateX_Test.kt)
- [X] [Entity_Create_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_Create_Test.kt)
- [X] [Entity_Delete_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_Delete_Test.kt)
- [X] [Entity_UpdateDescription_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_UpdateDescription_Test.kt)
- [X] [Entity_UpdateDocumentationHome_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_UpdateDocumentationHome_Test.kt)
- [X] [Entity_UpdateKey_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_UpdateKey_Test.kt)
- [X] [Entity_UpdateName_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_UpdateName_Test.kt)
- [ ] [Entity_UpdateX_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_UpdateX_Test.kt)
- [X] [Entity_XTag_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Entity_XTag_Test.kt)
- [X] [EntityAttribute_Create_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_Create_Test.kt)
- [X] [EntityAttribute_Delete_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_Delete_Test.kt)
- [X] [EntityAttribute_UpdateDescription_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_UpdateDescription_Test.kt)
- [X] [EntityAttribute_UpdateKey_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_UpdateKey_Test.kt)
- [X] [EntityAttribute_UpdateName_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_UpdateName_Test.kt)
- [X] [EntityAttribute_UpdateOptional_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_UpdateOptional_Test.kt)
- [X] [EntityAttribute_UpdateType_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_UpdateType_Test.kt)
- [X] [EntityAttribute_UpdateX_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_UpdateX_Test.kt)
- [X] [EntityAttribute_XTag_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityAttribute_XTag_Test.kt)
- [X] [EntityPrimaryKey_Update_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/EntityPrimaryKey_Update_Test.kt)
- [X] [Import_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Import_Test.kt)
- [X] [Model_Compare_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_Compare_Test.kt)
- [ ] [Model_Copy_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_Copy_Test.kt)
- [ ] [Model_Create_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_Create_Test.kt)
- [ ] [Model_Delete_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_Delete_Test.kt)
- [ ] [Model_Release_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_Release_Test.kt)
- [ ] [Model_UpdateAuthority_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_UpdateAuthority_Test.kt)
- [ ] [Model_UpdateDescription_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_UpdateDescription_Test.kt)
- [ ] [Model_UpdateDocumentationHome_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_UpdateDocumentationHome_Test.kt)
- [ ] [Model_UpdateKey_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_UpdateKey_Test.kt)
- [ ] [Model_UpdateName_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_UpdateName_Test.kt)
- [ ] [Model_UpdateX_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_UpdateX_Test.kt)
- [ ] [Model_XTag_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Model_XTag_Test.kt)
- [ ] [ModelAndTag_Event_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/ModelAndTag_Event_Test.kt)
- [ ] [Relationship_XTag_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Relationship_XTag_Test.kt)
- [ ] [RelationshipRole_Create_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/RelationshipRole_Create_Test.kt)
- [ ] [RelationshipRole_Delete_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/RelationshipRole_Delete_Test.kt)
- [ ] [RelationshipRole_UpdateCardinality_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/RelationshipRole_UpdateCardinality_Test.kt)
- [ ] [RelationshipRole_UpdateEntity_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/RelationshipRole_UpdateEntity_Test.kt)
- [ ] [RelationshipRole_UpdateKey_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/RelationshipRole_UpdateKey_Test.kt)
- [ ] [RelationshipRole_UpdateName_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/RelationshipRole_UpdateName_Test.kt)
- [ ] [RelationshipRole_UpdateX_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/RelationshipRole_UpdateX_Test.kt)
- [ ] [Search_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Search_Test.kt)
- [ ] [SearchFixture.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/SearchFixture.kt)
- [ ] [SearchFixture.md](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/SearchFixture.md)
- [ ] [test_utils.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/test_utils.kt)
- [ ] [Type_Create_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Type_Create_Test.kt)
- [ ] [Type_Delete_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Type_Delete_Test.kt)
- [ ] [Type_UpdateDescription_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Type_UpdateDescription_Test.kt)
- [ ] [Type_UpdateName_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Type_UpdateName_Test.kt)
- [ ] [Type_UpdateX_Test.kt](../extensions/models-core/src/test/kotlin/io/medatarun/model/actions/Type_UpdateX_Test.kt)

## Status (tags-core)

TODO

## Status (auth-core)

TODO