# TODO `models-core`

Ce fichier liste uniquement le travail restant et les écarts entre le contrat
cible du module (décrit dans `README.md`) et l'état actuel du code.

Ce qui est implémenté doit rester dans le README.
Ce qui ne l'est pas encore doit être suivi ici, puis supprimé une fois livré.

## 1) Actions `ModelAction` non implémentées dans `ModelActionProvider`

État actuel visible:

- `Model_UpdateKey`: non implémenté
- `RelationshipRole_Create`: non implémenté
- `RelationshipRole_UpdateKey`: non implémenté
- `RelationshipRole_UpdateName`: non implémenté
- `RelationshipRole_UpdateEntity`: non implémenté
- `RelationshipRole_UpdateCardinality`: non implémenté
- `RelationshipRole_Delete`: non implémenté

Attendu cible:

- `ModelActionProvider` route aussi ces actions vers des commandes métier
  (`ModelCmds` / `ModelQueries`) comme les autres actions déjà branchées.

## 2) Éviter les tags dupliqués sur un même objet

Écart actuel à traiter:

- un même objet (`Model`, `Entity`, `Relationship`, `Attribute`) ne doit pas
  pouvoir recevoir deux fois le même tag (`TagId`)

## 3) Comparaison

### 3.1 Verrouiller le contrat JSON de `ModelAction.Compare` par tests

- écrire des assertions explicites sur les champs racine attendus:
  - `scopeApplied`, `left`, `right`, `entries`
  - `left/right`: `modelId`, `modelKey`, `modelVersion`, `modelAuthority`
- écrire des assertions explicites sur chaque entrée:
  - `status`, `objectType`, `modelKey`, `typeKey`, `entityKey`,
    `relationshipKey`, `roleKey`, `attributeKey`, `left`, `right`
  - règle de présence:
    - `ADDED` => `left = null`, `right != null`
    - `DELETED` => `left != null`, `right = null`
    - `MODIFIED` => `left != null`, `right != null`
- couvrir les cas limites JSON:
  - comparaison de deux modèles avec clés différentes,
  - objets ajoutés/supprimés/modifiés,
  - ordre stable des entrées pour éviter les diffs bruités.

### 3.3 Refaire les tests en scénarios métier

- transformer les tests unitaires actuels trop techniques en tests orientés
  métier sur les comportements attendus.
- définir un vrai plan de test de comparaison orienté métier, puis l'implémenter
  dans `Model_Compare_Test` via `ModelTestEnv` (flux léger de bout en bout).
- inclure explicitement la validation des règles déjà actées:
  - `keys` et changements de `type` => `structural`
  - `version`, `origin`, `authority`, `documentationHome`, `tags`, textes => `complete`
- couvrir explicitement:
  - `structural` vs `complete`,
  - modifications de structure (type, optionalité, cardinalité, identifier),
  - modifications de contenu (textes, tags, documentation),
  - ajouts/suppressions sur types, entités, relations, attributs et rôles.

### 3.4 Terminer UI `ModelCompare`

#### 3.4.1 Compléter le rendu de comparaison

- remplacer l'affichage temporaire JSON seul par une vraie vue de comparaison
  gauche/droite pour aider la lecture des différences.

#### 3.4.2 Finaliser la disposition du formulaire

- afficher les champs du formulaire de comparaison sur une seule ligne
  (modèle gauche, modèle droit, comparison mode, bouton comparer).
