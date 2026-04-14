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