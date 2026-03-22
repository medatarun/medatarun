# Versionning - Journal De Decision (Q/R)

Ce fichier garde uniquement:

- ce qui est explicitement validé,
- ce qui reste ouvert.

## Besoin validé

Le but du système est de fournir un modèle de domaine explicite, vivant, partagé
et stable pour que métiers, dev, ops, gouvernance, sécurité et agents IA
travaillent sur la
même réalité, au lieu de vues fragmentées.

Le système doit avoir un vrai versionning des modèles pour pouvoir affirmer sans
ambiguïté:

- "en v1 le modèle est X",
- "en v2 le modèle est Y",
- "en v3 le modèle est Z".

Le système doit aussi permettre de comprendre précisément comment on est passé
d'une version à la suivante et qui a fait quoi.

Le versionning demandé est un levier produit pour cette promesse:

- rendre les versions du modèle fiables et stables dans le temps;
- rendre les évolutions explicables (ce qui a changé et pourquoi);
- permettre des opérations humaines et agents sur une base commune auditable;
- éviter que l'alignement entre équipes dépende d'interprétations ou d'artefacts
  dispersés.

Contexte de livraison:

- le produit est en phase de conception initiale,
- il n'existe pas de migration legacy à gérer dans ce cadrage.

## Comment (validé)

Dans l'application un `model` représente un bounded context au sens DDD.
Il contient des `entity`, `entity attribute`, `relationship`,
`relationship role`, `relationship attribute`, `type`.
Le principe est que le `model` et ses éléments portent un sens métier par le
biais
d'un nommage humain, descriptions métiers, tags.

La structure technique visée repose sur une architecture à base d'event log (
appelée
ici `model_event`) et de snapshots temporels (`model_snapshot`).

## Vocabulaire

- `model`: identité stable absolue de "ce modèle", indépendante des événements
  et des snapshots.
- `model_event`: historique des changements, source de vérité.
- `release`: événement métier sérialisé dans `model_event` avec
  `event_type = model_release`, qui marque la création d'une version acceptée
  par `ModelVersion` et sa frontière historique.
- ce qu'on appelle une action de création de release, ou de release, est
  l'émission d'un `model_event` avec `event_type = model_release`
- `version`: valeur acceptée par `ModelVersion` (sous ensemble de SemVer, comparable), portée par un `model_event`
  avec `event_type = model_release`. Une version nomme une frontière historique
  stable du modèle. Une version n'est ni un état métier autonome, ni un
  snapshot.
- `model_snapshot`: projection dérivée des `model_event`.
- `snapshot_kind`: propriété d'un `model_snapshot` qui peut valoir
  `CURRENT_HEAD` (dernier état connu, mutable, recalculable) ou
  `VERSION_SNAPSHOT` (snapshot immuable rattaché à un `model_event` de type
  `model_release`).
- `stream_revision`: ordre canonique monotone des `model_event` pour un `model`.

## Validé (explicite)

### Vision globale

- Le système doit reconstruire exactement l'état complet d'un modèle pour toute
  version (`v1`, `v2`, `v3`) créée par un `model_event` avec
  `event_type = model_release`.
- Le système doit expliquer précisément les changements entre versions.
- Une version créée par un `model_event` avec `event_type = model_release` est strictement
  immuable.
- Le flux est cumulatif et linéaire:
  `(rien) -> model_event -> ... -> release -> model_event -> ... -> release`.
- Le terme "brouillon" n'est pas retenu.

### Source de vérité et projections

- Les décisions BDD de ce chantier sont centralisées dans
  [DATABASE.md](./DATABASE.md).
- `model_event` est la source de vérité.
- La projection `CURRENT_HEAD` est un cache reconstruisible.
- `CURRENT_HEAD` représente l'état du modèle après application de tous les
  `model_event` jusqu'à la révision courante.
- Politique de snapshots: un snapshot immuable par release et un snapshot
  mutable `CURRENT_HEAD` mis à jour à chaque `model_event`.
- Le `model_snapshot` de type `VERSION_SNAPSHOT` fige toute la structure du modèle (
  model/types/entities/relationships/attributes/roles + tags attachés par
  `TagId`).
- Le `model_snapshot` de type `VERSION_SNAPSHOT` est une projection dérivée reconstructible depuis
  `model_event`.
- La version éventuellement stockée dans un snapshot est une dénormalisation,
  pas la source de vérité.
- Pour un `VERSION_SNAPSHOT`, cette valeur pratique reflète la version du
  `model_event` de release associé.
- Pour un `CURRENT_HEAD`, cette valeur pratique peut contenir la dernière
  version publiée connue, sans devenir la définition canonique de la version du
  modèle.
- Les lectures applicatives rapides s'appuient sur les projections.
- L'audit et la traçabilité s'appuient sur `model_event`, jamais sur les
  projections seules.
- Unicité du snapshot courant: exactement un `model_snapshot` de type
  `CURRENT_HEAD` par `model`.
- Tous les snapshots liés à une release utilisent
  `snapshot_kind = VERSION_SNAPSHOT`.

### Publication de release

- La création d'une release est un `model_event` métier dédié.
- Le `model_event` avec `event_type = model_release` porte les métadonnées (au
  minimum version,
  auteur, date) et définit la frontière historique.
- Pas de redondance de frontière de release (`last_event_id` séparé non retenu
  si la frontière est déjà portée par le `model_event` de release).
- La création d'une release ne peut se faire qu'à partir du `CURRENT_HEAD` du
  `model`.
- Mode strict: la création d'une release est refusée si la cohérence
  reconstruction `model_event -> CURRENT_HEAD` échoue.
- Atomicité: création du `model_event` de release et du `model_snapshot` de type `VERSION_SNAPSHOT` dans
  la même transaction logique.
- Idempotence: un rejeu de la même demande de création de release ne crée pas de
  doublon. Deux `model_event` ne peuvent pas avoir le même numéro de version. 
- Initialisation create/import:
  - `create` et `import` produisent des `model_event` de contenu,
  - Le projecteur construit/met à jour `CURRENT_HEAD` au fil de ces events.
  - puis un `model_event` avec `event_type = model_release` est créé dans le
    flux initial, à partir du numéro indiqué dans le `create`ou `import`
  - un `model_snapshot` de type `VERSION_SNAPSHOT` est créé à partir du `CURRENT_HEAD`.
  - ainsi `CURRENT_HEAD` existe tout le temsp, y compris avant le premier event `release`
  - atomicité exacte de toute la séquence (tout ou rien) : c'est dans une transaction
  - pas de retry/timeout (c'est synchrone en BDD dans la meme base)

### Ordonnancement et concurrence

- Ordonnancement canonique des `model_event` par `model` via `stream_revision`
  monotone.
- `stream_revision` est une donnée métier attribuée transactionnellement, pas un
  auto-increment global du moteur SQL.
- Contrat de concurrence d'écriture: append avec `expected_revision` et
  contrainte d'unicité `(model_id, stream_revision)`.
- `prev_event_id` n'est pas retenu en V1 pour éviter une double source de vérité
  de l'ordre.

### Règles de version

- Attribution de version en V1: la version est choisie explicitement par
  l'utilisateur au moment de la création de release.
- Format imposé: les versions acceptées sont celles supportées par
  `ModelVersion`.
- Les versions doivent être naturellement comparables via l'ordre défini par
  `ModelVersion`.
- Saut de versions autorisé (ex: `1.2.0` vers `3.0.0`) tant que la version est
  valide, comparable et unique par `model`.
- Les pré-releases acceptées ou non sont celles acceptées ou non par
  `ModelVersion`.
- Build metadata SemVer (`+build...`) non autorisé en V1 car non accepté par
  `ModelVersion`.
- La version initiale d'un `model` est libre à la création (y compris import),
  tant qu'elle est acceptée par `ModelVersion`.
- Chaque release suivante doit avoir une version unique et strictement
  supérieure à la précédente pour le même `model` selon l'ordre défini par
  `ModelVersion`.
- Unicité stricte de version par `model`.

### Identité métier

- Les objets versionnables du graphe (`type/entity/attribute/relationship/role`)
  portent un identifiant stable à travers le temps, indépendant de leur clé
  métier.
- `ModelKey` est renommable.
- `model.id` porte l'identité absolue.
- La `key` métier du `model` fait partie de l'état versionné: elle est portée
  par les projections de snapshot, pas par la table d'identité `model`.
- Règle d'unicité: à un instant donné, la `key` courante d'un modèle doit être
  unique globalement entre tous les `CURRENT_HEAD`.

### Sécurité, traçabilité et opérations

- Chaque `model_event` doit porter l'identité de l'actor qui a initié l'action.
- Chaque `model_event` doit porter un `traceability_origin` système permettant
  d'identifier l'action source via le mécanisme de tracabilité. 
- `traceability_origin` est une string avec `action:<UUIDv7>` attribué par la plateforme d'actions (`actionInstanceId` dans le langage des actions).
- Les métadonnées `actor_id` et `traceability_origin` doivent être propagées de bout en
  bout: `ModelAction` -> `ModelActionProvider` -> `ModelCmdsImpl` ->
  `ModelStorage` -> `ModelRepoCmd` -> `ModelStorageDb` -> `model_event`.
- Le point d'entrée unique du logiciel, tous canaux confondus (UI, API, MCP),
  est le système d'actions. Il n'existe pas de chemin alternatif pour écrire
  des `model_event`.
- La sérialisation des commandes vers `model_event` doit utiliser un mapping
  explicite et stable pour chaque commande de `ModelRepoCmd.kt`.
- Ce mapping ne doit pas persister les noms de classes/propriétés Kotlin tels
  quels; il doit utiliser des noms choisis et stables (annotations dédiées).
- `model_event` est append-only en flux normal; la correction passe par des
  événements compensatoires.
- Réparabilité manuelle exigée en incident: correction SQL manuelle possible
  avec des outils classiques.
- Cette réparabilité n'est pas une fonctionnalité à implémenter dans le
  logiciel.
- En cas d'arbitrage, la robustesse logicielle prime sur la simplicité de
  réparation manuelle.
- Pas de contrôle périodique automatique en V1 (pas de timer interne).
- Contrôle de cohérence en V1: obligatoire à la création d'une release et
  possible via une action manuelle dédiée de vérification/reconstruction.
- Tradeoff accepté: la reconstruction de `CURRENT_HEAD` peut être longue en
  incident si beaucoup de `model_event` se sont accumulés.
- Suppression technique d'un `model` par un administrateur:
  - une commande de suppression est envoyée par le domaine au storage comme les autres commandes
  - elle n'est pas considérée comme un event, elle implique une suppression totale du `model`, de ses `model_event` et de ses `model_snapshot` en cascade.

### Serialisation des events

- Chaque event a un event_type explicite et immuable (ex: entity_attribute_added), jamais le nom de classe Kotlin.
- Chaque event a un schema_version entier dans l’enveloppe.
- Payload JSON avec champs nommés explicitement et stables en utilisant les annotations des data classe, Pas de sérialisation auto des data classes non annotées. Une classe ne peut pas être sérialisée si elle n'a pas les annotations qui figent le nommage.
- Ajouter occurred_at, actor_id, traceability_origin, model_id, stream_revision dans l’enveloppe, pas dans le payload métier.
- Règle: ajout de champ = compatible; renommage/suppression = nouvelle schema_version.
- Registry explicite: (event_type, schema_version) -> decoder.
- Tests unitaires de round-trip et upcast figés écrits JSON (pas en dataclass) pour tous les events.
- Ne jamais réutiliser un event_type pour un sens différent.
- Si la sémantique change, créer un nouveau event_type ou bump de version majeur du schéma d’event.
- Documenter chaque changement dans un “Event Contract Changelog”.

Migration d'events

- Migration à la lecture via upcasters qui transforment un event v1 en v2 en v3 jusqu'au format courant avant application
- Garder les anciens décodeurs côté application tant que des events historiques existent

### Tags et restitution historique

- Les releases et snapshots conservent les `TagId` attachés aux objets du
  modèle.
- Les releases et snapshots ne figent pas les métadonnées complètes des tags
  (`key`, `name`, `description`, etc.).
- Le fait historique conservé par le versionning des modèles est donc
  l'attache d'un `TagId` à un objet, pas l'état complet du tag dans
  `tags-core`.
- Ce choix est retenu parce que le sens principal du modèle est porté par les
  noms et descriptions des objets versionnés. Les tags portent surtout des
  rattachements de classement et des marquages transverses.
- Le cycle de vie des tags reste autonome. Une release historique de
  `models-core` ne bloque ni la suppression ni l'évolution d'un tag dans
  `tags-core`.

Conséquences explicites:

- si un tag référencé historiquement est supprimé dans `tags-core`, une
  remontée d'historique peut ne plus restituer que le `TagId` stocké;
- si un tag est renommé, la remontée d'historique affiche sa `key` actuelle, pas
  celle du moment de la release;
- si le `name` ou la `description` d'un tag changent, la remontée d'historique
  affiche les valeurs actuelles, pas celles du moment de la release;
- cette perte de restitution complète du tag ne remet pas en cause la validité
  historique de la release du modèle.

## Hors sujet (pour ce document)

Le design UX détaillé n'est pas dans le périmètre de ce document.

Point acté pour éviter les ambiguïtés:

- une action de création de release sera disponible côté application,
- cette action utilisera un formulaire dédié.
- les droits d'accès restent gérés au niveau des actions via règles de
  sécurité (roles/permissions),
  et ne sont pas redéfinis par la logique de versionning.
- la politique détaillée "qui peut supprimer quoi" et la traçabilité associée
  (action log) existent déjà au niveau application et ne sont pas redéfinies ici.
- l'application porte plusieurs notions de diff (métier: drift/suivi
  d'évolution/comparaison de modèles, gouvernance générale, audit des
  modifications), avec des traitements différents.
- ce chantier ne définit pas la manière de traiter ces diff; il définit
  uniquement les fondations (`model_event` + snapshots) qui les rendent possibles.
- le cycle de vie métier des modèles (actif/inactif/visibilité) n'est pas défini
  dans ce chantier.
- Gestion fonctionnelle des conflits de concurrence.
  Le contrôle `expected_revision` est validé, le comportement utilisateur
  en cas de conflit n'est pas dans ce périmètre, on sait juste qu'une exception sera levée.


Le contenu exact des écrans, des parcours et des interactions UX sera traité
dans un travail séparé.

## Risques connus

### Suppression de tags

Si un `model_event` référence un `TagId` puis que ce tag est supprimé côté
`tags-core`, la reconstruction structurelle reste possible, mais la restitution
sémantique (`key/name/description`) peut devenir partielle si ces métadonnées ne
sont pas conservées historiquement.

### Blocage de publication stricte

La création d'un event `model_release` peut échouer en cas d'incident de
cohérence (projecteur buggué, migration défectueuse, corruption, validation
incomplète d'event). Ce blocage est volontaire pour éviter la création d'une
version non reconstruisible.

## Contexte technique observé (non décisionnel)

- `ModelAggregate` porte un état complet (model, types, entities, relationships,
  attributes, tags).
- La couche commandes exprime déjà des changements métier détaillés (`Create*`,
  `Update*`, `Delete*`).
- Le stockage actuel applique surtout des mutations en place de l'état courant.
