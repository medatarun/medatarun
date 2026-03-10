# DATABASE - Spécification SQL (V1)

Ce document fige la cible SQL du chantier versionning.

Il ne décrit pas l'UX ni les politiques produit hors base.

## 1) Tables maître

### `model`

Rôle: identité stable du modèle.

Colonnes:
- `id` TEXT PRIMARY KEY: identifiant technique stable du modèle.

### `model_event`

Rôle: source de vérité append-only.

Colonnes:
- `id` TEXT PRIMARY KEY: identifiant technique unique de l'event.
- `model_id` TEXT NOT NULL REFERENCES `model(id)` ON DELETE CASCADE: modèle
  auquel l'event appartient.
- `stream_revision` INTEGER NOT NULL: ordre canonique de l'event dans le flux du
  modèle.
- `event_type` TEXT NOT NULL: type d'event (`create`, `update`, `release`, etc.).
- `version` TEXT NULL: version SemVer portée uniquement pour les events
  `event_type='release'`.
- `actor_id` TEXT NOT NULL: identifiant stable de l'acteur qui a initié l'action.
- `action_id` TEXT NOT NULL: identifiant système de l'action source, fourni par
  la plateforme d'actions.
- `payload` TEXT NOT NULL: contenu détaillé de l'event (JSON texte).
- `created_at` TEXT NOT NULL: date/heure de création de l'event.

Règle de mapping:
- `event_type` et le `payload` sont dérivés des commandes décrites dans
  `ModelRepoCmd.kt`.
- Le mapping doit être explicite et stable; il ne doit pas dépendre
  implicitement des noms de classes/propriétés Kotlin.
- `actor_id` ne porte pas de FK vers le module auth (pas de contrainte
  cross-zone).
- `action_id` vient des identifiants d'actions de la plateforme et sert la
  traçabilité opérationnelle (relier un `model_event` à l'action source).

Contraintes:
- `UNIQUE(model_id, stream_revision)` (ordre canonique)
- `UNIQUE(model_id, version)` avec filtre logique `event_type='release'`
  si le moteur SQL le permet; sinon garantir l'unicité côté application.

### `model_snapshot`

Rôle: projection matérialisée.

Colonnes:
- `id` TEXT PRIMARY KEY: identifiant technique du snapshot.
- `model_id` TEXT NOT NULL REFERENCES `model(id)` ON DELETE CASCADE: modèle
  auquel le snapshot appartient.
- `key` TEXT NOT NULL: clé métier du modèle dans ce snapshot.
- `name` TEXT NOT NULL: nom du modèle dans ce snapshot.
- `description` TEXT NOT NULL: description du modèle dans ce snapshot.
- `origin` TEXT NOT NULL: source métier/technique du modèle dans ce snapshot.
- `authority` TEXT NOT NULL: autorité/référentiel de ce snapshot.
- `documentation_home` TEXT NULL: lien de documentation racine du modèle dans
  ce snapshot.
- `snapshot_kind` TEXT NOT NULL CHECK (`snapshot_kind` IN ('CURRENT_HEAD', 'VERSION_SNAPSHOT')):
  nature du snapshot (`CURRENT_HEAD` mutable ou `VERSION_SNAPSHOT` figé).
- `up_to_revision` INTEGER NOT NULL: dernière `stream_revision` incluse dans ce
  snapshot.
- `release_event_id` TEXT NULL REFERENCES `model_event(id)` ON DELETE CASCADE:
  event `release` associé (obligatoire pour `VERSION_SNAPSHOT`, null pour
  `CURRENT_HEAD`).
- `version` TEXT NULL: version SemVer liée au snapshot versionné (null pour
  `CURRENT_HEAD`).
- `created_at` TEXT NOT NULL: date/heure de création du snapshot.
- `updated_at` TEXT NOT NULL: date/heure de dernière mise à jour (utile surtout
  pour `CURRENT_HEAD`).

Contraintes:
- Un seul `CURRENT_HEAD` par `model` (index unique partiel recommandé).
- Un seul `VERSION_SNAPSHOT` par `release_event_id`.
- `version` obligatoire pour `VERSION_SNAPSHOT`, null pour `CURRENT_HEAD`
  (CHECK si supporté, sinon règle applicative).
- Unicité de la clé courante: `key` doit être unique globalement entre tous les
  snapshots `CURRENT_HEAD` (index unique partiel recommandé).

## 2) Tables projection métier

Principe: toutes les tables actuellement rattachées à `model_id` sont
rattachées à `snapshot_id`.

Exemples cibles:
- `snapshot_model_tag`: tags du modèle pour un `snapshot_id`.
- `snapshot_model_type`: types du modèle pour un `snapshot_id`.
- `snapshot_entity`: entités pour un `snapshot_id`.
- `snapshot_entity_tag`: tags des entités pour un `snapshot_id`.
- `snapshot_entity_attribute`: attributs d'entité pour un `snapshot_id`.
- `snapshot_entity_attribute_tag`: tags des attributs d'entité pour un
  `snapshot_id`.
- `snapshot_relationship`: relations pour un `snapshot_id`.
- `snapshot_relationship_tag`: tags des relations pour un `snapshot_id`.
- `snapshot_relationship_role`: rôles de relation pour un `snapshot_id`.
- `snapshot_relationship_attribute`: attributs de relation pour un `snapshot_id`.
- `snapshot_relationship_attribute_tag`: tags des attributs de relation pour un
  `snapshot_id`.

Règle d'unicité à appliquer:
- les clés métier qui étaient uniques par `model_id` deviennent uniques par
  `snapshot_id` (ou par owner dans le snapshot).

## 3) Écriture transactionnelle (contrat SQL)

### Append d'un `model_event`

Entrées: `model_id`, `expected_revision`, `event_type`, `payload`, métadonnées.

Contrat:
1. vérifier que la dernière révision du modèle est `expected_revision`,
2. insérer le nouvel event avec `stream_revision = expected_revision + 1`,
3. projeter l'event dans `CURRENT_HEAD`,
4. commit.

En cas de concurrence: échec sur unicité `(model_id, stream_revision)` ou
vérification de précondition, puis retry applicatif.

### Création d'une release

Contrat atomique:
1. insérer `model_event(event_type='release', version=...)`,
2. mettre à jour `CURRENT_HEAD` jusqu'à cette révision,
3. créer `VERSION_SNAPSHOT` depuis `CURRENT_HEAD`,
4. commit.

## 4) Contrat create/import

`create`/`import` doivent aboutir à:
- un flux initial de `model_event` de contenu,
- un `CURRENT_HEAD` à jour,
- un `model_event` de type `release` avec version initiale,
- un `VERSION_SNAPSHOT` correspondant.

## 5) Recherche

Les tables denorm de recherche doivent être rattachées au snapshot lu:
- recherche courante: `CURRENT_HEAD`,
- recherche sur version: `VERSION_SNAPSHOT` ciblé.

## 6) Suppression technique

Suppression d'un `model`:
- suppression totale du `model`,
- suppression en cascade des `model_event`,
- suppression en cascade des `model_snapshot`,
- suppression en cascade des tables projection rattachées aux snapshots.

## 7) Points SQL à finaliser

- Noms définitifs des tables `snapshot_*` (alignement avec conventions existantes).
- DDL exact des CHECK/indices partiels selon moteur (SQLite cible V1).
- Format exact de `payload` (`JSON` texte) et indexation éventuelle.
