# TODO `models-core`

Ce fichier liste uniquement le travail restant et les écarts entre le contrat
cible du module (décrit dans `README.md`) et l'état actuel du code.

Ce qui est implémenté doit rester dans le README.
Ce qui ne l'est pas encore doit être suivi ici, puis supprimé une fois livré.

## [EVENT_CODEC_URL_WARNING] Supprimer l'usage de `URL(String)` déprécié

Règle technique cible:

- le code du module ne doit pas utiliser le constructeur Java `URL(String)`
  désormais déprécié.
- les conversions de chaînes vers `URL` doivent passer par `URI(...).toURL()`
  ou une autre construction explicite équivalente.

Constat actuel dans le code:

- le codec d'events `ModelRepoCmdEventJsonCodec.kt` utilise encore `URL(String)`
  dans son serializer dédié des `URL`.
- le test `ModelRepoCmdEventJsonCodecTest.kt` utilise aussi `URL(String)` dans
  ses fixtures.
- tout fonctionne, mais la compilation remonte des warnings de dépréciation.

Ce qui manque encore:

- remplacer les constructions `URL(String)` dans
  `ModelRepoCmdEventJsonCodec.kt`.
- remplacer les constructions `URL(String)` dans
  `ModelRepoCmdEventJsonCodecTest.kt`.
- vérifier ensuite que `:extensions:models-core:compileKotlin` et
  `:extensions:models-core:test` ne remontent plus ce warning.

## [TAG_DEDUP_RULE] Éviter les tags dupliqués sur un même objet

Règle métier cible:

- un même objet (`Model`, `Entity`, `Relationship`, `Attribute`) ne doit pas
  pouvoir recevoir deux fois le même tag (`TagId`).

Constat actuel dans le code:

- comportement déjà idempotent au niveau repository DB:
  - `addModelTag`, `addEntityTag`, `addEntityAttributeTag`,
    `addRelationshipTag`, `addRelationshipAttributeTag`
    vérifient l'existence `(objet, tag)` avant insertion.
- donc en pratique, un double `AddTag` successif ne crée pas de doublon
  dans le flux applicatif actuel.

Ce qui manque encore pour considérer la règle verrouillée:

- ajouter une contrainte d'unicité au schéma SQL sur les tables de liaison
  tags (au minimum en clé composite):
  - `model_tag(model_id, tag_id)`
  - `entity_tag(entity_id, tag_id)`
  - `entity_attribute_tag(attribute_id, tag_id)`
  - `relationship_tag(relationship_id, tag_id)`
  - `relationship_attribute_tag(attribute_id, tag_id)`
- ajouter dans la création de DB pour introduire ces contraintes sans casser
  les bases existantes (gestion préalable d'éventuels doublons historiques).
- garder la garde applicative actuelle (check `exists`) même après contrainte DB
  pour préserver le comportement idempotent côté commande.

Couverture tests actuelle:

- les tests `Model_XTag_Test`, `Entity_XTag_Test`, `EntityAttribute_XTag_Test`,
  `Relationship_XTag_Test` couvrent surtout:
  - ajout/suppression nominal,
  - règles de scope (`TagAttachScopeMismatchException`).
- pas de test explicite trouvé qui vérifie le scénario:
  - "ajouter deux fois le même tag sur le même objet => une seule occurrence".

Travail test restant:

- ajouter des tests métier explicites sur le double ajout pour chacun des 5
  niveaux d'attache tag (`Model`, `Entity`, `EntityAttribute`, `Relationship`,
  `RelationshipAttribute`).
- vérifier à la fois:
  - l'absence de doublon en lecture (`tags` ne contient qu'une occurrence),
  - et la stabilité comportementale (pas d'exception inattendue sur le second
    ajout).

## [ENTITY_DELETE_RELATION_GUARD] Suppression d'entité utilisée dans une relation

Règle métier cible:

- interdire explicitement la suppression d'une entité si elle est encore
  référencée par au moins un rôle de relation.

Constat actuel dans le code:

- `ModelCmd.DeleteEntity` existe et `ModelCmdsImpl.deleteEntity(...)` supprime
  directement via `ModelRepoCmd.DeleteEntity` sans vérification métier
  préalable d'usage dans les relations.
- en pratique, la base bloque via contrainte de clé étrangère si des rôles de
  relation pointent encore vers l'entité.
- le blocage actuel dépend donc d'une contrainte technique DB, pas d'une règle
  métier explicite dans le domaine.

Ce qui manque encore:

- ajouter une vérification métier explicite dans `ModelCmdsImpl` avant
  `DeleteEntity` (via une requête repository dédiée pour détecter des rôles de
  relation référents).
- lever une exception métier dédiée (classe spécifique) quand l'entité est
  encore utilisée.
- conserver la contrainte DB comme filet de sécurité, mais ne plus dépendre
  d'elle pour exprimer la règle métier.

Couverture tests actuelle:

- `Entity_Delete_Test` couvre la suppression nominale et l'isolation entre
  modèles.
- aucun test explicite trouvé pour le cas:
  - "suppression d'une entité utilisée dans une relation => erreur métier".

Travail test restant:

- ajouter un test métier qui crée une relation pointant vers l'entité puis
  vérifie que `Entity_Delete` échoue avec l'exception métier dédiée.
- ajouter un test qui confirme que la suppression redevient possible après
  suppression/adaptation des rôles/relations concernés.

## [MODEL_COMPARE] Comparaison

### [COMPARE_JSON_CONTRACT] Verrouiller le contrat JSON de `ModelAction.Compare` par tests

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

### [COMPARE_BUSINESS_TESTS] Refaire les tests en scénarios métier

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

### [COMPARE_UI_VARIANTS] Repenser l'UX de lecture du diff (écran `Model compare`)

Contexte actuel et pourquoi on s'arrête ici pour l'instant:

- la version actuelle de `ModelCompareDiffView.tsx` est opérationnelle, permet
  de comparer deux modèles et d'identifier les changements.
- malgré plusieurs itérations (couleurs, hiérarchie, compactage, open/close),
  la lecture reste coûteuse sur des cas réels.
- la difficulté principale vient du fait que le diff actuel essaie de forcer
  une représentation "tableau 3 colonnes façon git" sur une structure métier
  arborescente (modèle -> entités/relations -> attributs/rôles -> propriétés).
- conséquence: même quand le rendu est techniquement correct, la compréhension
  "métier" reste partiellement ambiguë.

Défauts constatés sur la version actuelle:

- ambiguïtés hiérarchiques: certains niveaux semblent au même rang visuel
  alors qu'ils ne portent pas la même signification métier (objet parent vs
  enfant, conteneur vs vrai changement).
- surcharge visuelle: beaucoup de chrome d'interface (lignes, fonds, pastilles,
  toggles) avant d'arriver à l'information qui répond à "qu'est-ce qui a
  changé concrètement ?".
- lisibilité hétérogène selon la profondeur: les changements simples sont
  lisibles, mais les cas riches (description, tags, multiples relations)
  deviennent longs à scanner.
- compromis non stabilisé entre densité, hiérarchie et lisibilité: toute
  amélioration locale dégrade souvent un autre aspect.

Pistes validées à explorer (nouveaux rendus alternatifs):

- `Piste A - Changelog métier`
  - objectif: afficher des phrases d'événements compréhensibles directement
    ("Entité X ajoutée", "Attribut Y supprimé", "Description modifiée").
  - usage cible: lecture rapide, communication, résumé de changement.
  - format attendu: liste structurée par catégories (modèle, entités,
    relations), avec avant/après inline uniquement quand nécessaire.
  - bénéfice attendu: réduction forte du coût cognitif pour les profils non
    techniques.

- `Piste B - Arbre de navigation + panneau de détail`
  - objectif: séparer navigation et comparaison détaillée.
  - usage cible: exploration précise d'un changement sans noyer l'utilisateur.
  - format attendu:
    - panneau gauche: arbre compact des éléments modifiés,
    - panneau droit: détail left/right de l'élément sélectionné.
  - bénéfice attendu: meilleure maîtrise des gros volumes et meilleure
    contextualisation de chaque changement.

Stratégie de mise en oeuvre (sans casser l'écran existant):

- créer des composants distincts, dans des fichiers parallèles à
  `ModelCompareDiffView.tsx`, pour isoler les variantes:
  - `ModelCompareDiffView_Current.tsx` (copie de la version actuelle),
  - `ModelCompareDiffView_Changelog.tsx` (piste A),
  - `ModelCompareDiffView_TreeDetails.tsx` (piste B).
- créer un composant de switch de variante (temporaire, non final produit):
  - `ModelCompareDiffVariantSwitcher.tsx`.
- faire le routage de rendu depuis `ModelComparePage.tsx` pour pouvoir basculer
  entre variantes sans toucher au backend ni au contrat `ModelCompareDto`.
- conserver strictement le même input (`ModelCompareDto`) pour les trois
  rendus afin de comparer les approches à périmètre fonctionnel identique.
- décider ensuite la vue cible et supprimer les variantes non retenues.

Critères de décision pour choisir la vue finale:

- temps de compréhension d'un diff réel (petit, moyen, volumineux).
- capacité à répondre vite à 3 questions:
  - quoi a été ajouté ?
  - quoi a été supprimé ?
  - quoi a été modifié et où ?
- absence d'ambiguïté hiérarchique perçue.
- capacité à servir à la fois:
  - un usage "résumé / message",
  - un usage "analyse détaillée".
