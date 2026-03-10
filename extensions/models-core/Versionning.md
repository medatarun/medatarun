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

- rendre les états publiés du modèle fiables et stables dans le temps;
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

La structure technique visée repose sur une logique d'event log et de snapshots.

## Vocabulaire

- `model`: identité stable absolue de "ce modèle", indépendante des événements
  et des snapshots.
- `model_event`: historique des changements, source de vérité.
- `release`: publication versionnée immuable, matérialisée par un `model_event`
  dédié.
- `model_snapshot`: projection dérivée des `model_event`.
- `snapshot_kind`: `HEAD` (dernier état connu, mutable, recalculable) ou
  `RELEASE` (état publié, immuable).
- `stream_revision`: ordre canonique monotone des `model_event` pour un `model`.
- `expected_revision`: valeur de contrôle de concurrence fournie lors d'un
  append de `model_event`.

## Validé (explicite)

### Vision globale

- Le système doit reconstruire exactement l'état complet d'un modèle pour toute version publiée (`v1`, `v2`, `v3`).
- Le système doit expliquer précisément les changements entre versions.
- Une version publiée est strictement immuable.
- Le flux est cumulatif et linéaire: `(rien) -> model_event -> ... -> release -> model_event -> ... -> release`.
- Le terme "brouillon" n'est pas retenu.

### Source de vérité et projections

- `model_event` est la source de vérité.
- La projection `head/current` est un cache reconstruisible.
- L'état courant projeté est égal à la dernière release plus les événements survenus depuis cette release.
- Politique de snapshots: un snapshot immuable par release et un snapshot mutable `head/current` mis à jour à chaque `model_event`.
- Le snapshot de release fige toute la structure du modèle (model/types/entities/relationships/attributes/roles + tags attachés par `TagId`).
- Le snapshot de release est une projection dérivée reconstructible depuis `model_event`.
- La version éventuellement stockée dans un snapshot est une dénormalisation, pas la source de vérité.
- Les lectures applicatives rapides s'appuient sur les projections.
- L'audit et la traçabilité s'appuient sur `model_event`, jamais sur les projections seules.
- Unicité du snapshot courant: exactement un `model_snapshot` de type `HEAD` par `model`.

### Publication de release

- Une publication de release est un `model_event` métier dédié.
- Le `model_event` de release porte les métadonnées de publication (au minimum version, auteur, date) et définit la frontière historique.
- Pas de redondance de frontière de release (`last_event_id` séparé non retenu si la frontière est déjà portée par le `model_event` de release).
- La publication ne peut se faire qu'à partir du `HEAD` courant du `model`.
- Publication sans changement interdite: impossible de publier si aucun `model_event` de contenu n'a été ajouté depuis la release précédente.
- Publication en mode strict: une release est refusée si la cohérence reconstruction `model_event -> HEAD` échoue.
- Atomicité de publication: création du `model_event` de release et du snapshot de release dans la même transaction logique.
- Publication idempotente: un rejeu de la même demande ne crée pas de doublon.

### Ordonnancement et concurrence

- Ordonnancement canonique des `model_event` par `model` via `stream_revision` monotone.
- `stream_revision` est une donnée métier attribuée transactionnellement, pas un auto-increment global du moteur SQL.
- Contrat de concurrence d'écriture: append avec `expected_revision` et contrainte d'unicité `(model_id, stream_revision)`.
- `prev_event_id` n'est pas retenu en V1 pour éviter une double source de vérité de l'ordre.

### Règles de version

- Attribution de version en V1: la version est choisie explicitement par l'utilisateur au moment de la publication.
- Format imposé: SemVer strict (`MAJOR.MINOR.PATCH`).
- Saut de versions autorisé (ex: `1.2.0` vers `3.0.0`) tant que SemVer est valide et unique par `model`.
- Pré-releases SemVer (`-alpha`, `-beta`, etc.) non autorisées en V1.
- Build metadata SemVer (`+build...`) non autorisé en V1.
- La version initiale d'un `model` est libre à la création (y compris import), tant qu'elle respecte SemVer.
- Chaque release suivante doit avoir une version unique et strictement supérieure à la précédente pour le même `model`.
- Unicité stricte de version par `model`.

### Identité métier

- Les objets versionnables du graphe (`type/entity/attribute/relationship/role`) portent un identifiant stable à travers le temps, indépendant de leur clé métier.
- `ModelKey` est renommable.
- `model.id` porte l'identité absolue.

### Sécurité, traçabilité et opérations

- Les droits d'accès sont traités au niveau des actions via des règles de sécurité (roles/permissions). Le versionning ne redéfinit pas le "qui peut".
- Une fois la règle de sécurité de l'action passée, le métier versionning considère l'opération autorisée jusqu'au stockage.
- Chaque `model_event` doit porter le nom de l'actor qui a initié l'action.
- Chaque `model_event` doit porter un `action_id` système permettant d'identifier l'action source.
- `model_event` est append-only en flux normal; la correction passe par des événements compensatoires.
- Réparabilité manuelle exigée en incident: correction SQL manuelle possible avec des outils classiques.
- Cette réparabilité n'est pas une fonctionnalité à implémenter dans le logiciel.
- En cas d'arbitrage, la robustesse logicielle prime sur la simplicité de réparation manuelle.
- Pas de contrôle périodique automatique en V1 (pas de timer interne).
- Contrôle de cohérence en V1: obligatoire à la publication et possible via une action manuelle dédiée de vérification/reconstruction.
- Tradeoff accepté: la reconstruction de `head/current` peut être longue en incident si beaucoup de `model_event` se sont accumulés.

## Questions ouvertes

1. Politique tags et reproductibilité historique.  
   Les releases figent la structure avec des `TagId`, mais les tags peuvent
   évoluer ou être supprimés dans `tags-core`. Il faut décider entre trois
   options: interdire la suppression d'un tag référencé par une release publiée,
   conserver les tags en suppression logique, ou autoriser la suppression
   physique en copiant les métadonnées de tags dans les snapshots de release.

2. Règles fonctionnelles de publication côté expérience utilisateur.  
   Les contraintes de cohérence techniques sont validées, mais l'expérience
   utilisateur avant confirmation n'est pas encore figée. Il faut préciser les
   informations obligatoires affichées avant publication, notamment le résumé
   des changements et la version saisie.

3. Parcours de comparaison entre releases.  
   L'objectif de traçabilité est validé, mais le contenu exact du diff métier
   reste à cadrer. Il faut décider le niveau de détail attendu pour les ajouts,
   suppressions, modifications et renommages, ainsi que la granularité
   d'affichage sur les objets du modèle.

4. Cycle de vie d'un `model` en fin de vie.  
   Le paradigme versionné est défini, mais les règles de suppression ou
   d'archivage ne sont pas tranchées. Il faut décider si la fin de vie passe par
   suppression physique, archivage logique, ou une combinaison, et définir
   l'impact sur `model_event`, `model_snapshot` et les releases.

5. Schéma SQL cible et plan d'initialisation V1.  
   Il n'y a pas de migration legacy à gérer, mais la structure finale reste à
   formaliser. Il faut définir les tables, les contraintes, les index et la
   séquence d'initialisation d'un nouveau modèle.

6. Contrat d'import vers l'historique versionné.  
   Les imports existent déjà, mais il faut formaliser comment un import crée
   les premiers `model_event`, le `HEAD`, et éventuellement une première
   release.

7. Gestion fonctionnelle des conflits de concurrence.  
   Le contrôle `expected_revision` est validé, mais le comportement utilisateur
   en cas de conflit reste à préciser (message affiché, rechargement, reprise).

## Risques connus

### Suppression de tags

Si un `model_event` référence un `TagId` puis que ce tag est supprimé côté
`tags-core`, la reconstruction structurelle reste possible, mais la restitution
sémantique (`key/name/description`) peut devenir partielle si ces métadonnées ne
sont pas conservées historiquement.

### Blocage de publication stricte

La publication stricte peut échouer en cas d'incident de cohérence (projecteur
buggué, migration défectueuse, corruption, validation incomplète d'event). Ce
blocage est volontaire pour éviter une release non reconstruisible.

## Contexte technique observé (non décisionnel)

- `ModelAggregate` porte un état complet (model, types, entities, relationships,
  attributes, tags).
- La couche commandes exprime déjà des changements métier détaillés (`Create*`,
  `Update*`, `Delete*`).
- Le stockage actuel applique surtout des mutations en place de l'état courant.
