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

1. Objectif principal: reconstruire exactement l'état complet d'un modèle pour
   une version publiée (`v1`, `v2`, `v3`).
2. Objectif complémentaire: expliquer précisément les changements entre deux
   versions.
3. Une version publiée est strictement immuable.
4. Le flux est cumulatif et linéaire:
   `(rien) -> model_event -> ... -> release -> model_event -> ... -> release`.
5. Le terme "brouillon" n'est pas retenu.
6. `model_event` est la source de vérité.
7. La projection `head/current` est un cache reconstruisible.
8. L'état courant projeté = dernière release + événements survenus depuis cette
   release.
9. Politique de snapshots:
    - un snapshot immuable par release,
    - un snapshot mutable `head/current` mis à jour à chaque `model_event`.
10. Tradeoff accepté: la reconstruction de `head/current` peut être longue en
    incident si beaucoup de `model_event` se sont accumulés.
11. Publication en mode strict:
- une release est refusée si la cohérence reconstruction `model_event -> HEAD`
  échoue.
12. `model_event` est append-only en flux normal:
- aucun event écrit n'est modifié ou supprimé,
- les corrections passent par des événements compensatoires.
13. Les objets versionnables du graphe (
    `type/entity/attribute/relationship/role`) portent un identifiant stable à
    travers le temps, indépendant de leur clé métier.
14. Réparabilité manuelle exigée en incident:
- correction SQL manuelle possible avec des outils classiques,
- ce n'est pas une fonctionnalité à implémenter dans le logiciel.
15. En cas d'arbitrage, la robustesse logicielle prime sur la simplicité de
    réparation manuelle.
16. Le snapshot de release doit figer toute la structure du modèle:
- model/types/entities/relationships/attributes/roles,
- tags attachés par `TagId`.
17. Une publication de release est un `model_event` métier dans le journal (
    marqueur temporel de publication).
18. Le `model_event` de release est la donnée maître pour la version:
- il porte les métadonnées de publication (au minimum version, auteur, date),
- il définit la frontière historique de la release.
19. Le snapshot de release est une projection dérivée:
- il est reconstructible depuis `model_event`,
- la version éventuellement stockée dans le snapshot est une dénormalisation,
  pas la source de vérité.
20. Les lectures applicatives (UI / requêtes rapides) s'appuient sur les
    projections.
21. L'audit et la traçabilité s'appuient sur la donnée maître (`model_event`),
    jamais sur les projections seules.
22. Pas de redondance de frontière historique sur la release:
- pas de champ additionnel de type `last_event_id` sur la release si cette
  information est déjà portée par le `model_event` de release.
23. Ordonnancement canonique des `model_event` par `model` via `stream_revision`
    monotone.
24. `stream_revision` est une donnée métier attribuée transactionnellement (pas
    un auto-increment global du moteur SQL).
25. Contrat de concurrence d'écriture:
- append avec `expected_revision`,
- contrainte d'unicité `(model_id, stream_revision)`.
26. `prev_event_id` n'est pas retenu en V1 pour éviter une double source de
    vérité de l'ordre.
27. Unicité stricte de version par modèle:
- impossible d'avoir deux releases avec le même numéro de version pour un même
  modèle.
28. Atomicité de publication:
- création du `model_event` de release et création du snapshot de release dans
  la même transaction logique.
29. Publication idempotente:
- une même demande de release rejouée ne doit pas créer de doublon et doit
  produire le même résultat observable.
30. Pas de contrôle périodique automatique en V1 (pas de timer interne).
31. Contrôle de cohérence en V1:
- obligatoire au moment de la publication (déjà couvert par la publication
  stricte),
- possible via une action manuelle dédiée de vérification/reconstruction.
32. Unicité du snapshot courant:
- exactement un `model_snapshot` de type `HEAD` par `model`.
33. Position de `ModelKey`:
- `ModelKey` est renommable,
- `model.id` porte l'identité absolue.
34. La publication d'une release ne peut se faire qu'à partir du `HEAD` courant
    du `model` (pas depuis un état antérieur).
35. Publication sans changement interdite:
- impossible de publier une nouvelle release si aucun `model_event` de contenu
  n'a été ajouté depuis la précédente release.
36. Attribution de version en mode mixte:
- le système propose une version,
- l'utilisateur valide ou ajuste avant publication.
37. Format de version imposé: SemVer strict (`MAJOR.MINOR.PATCH`).
38. La suggestion automatique de version (`major/minor/patch`) est déterminée
    par des règles basées sur les types de `model_event`.
39. Si l'utilisateur choisit un niveau de version inférieur à la suggestion
    système, la publication reste autorisée avec avertissement explicite.
40. Aucune justification textuelle n'est obligatoire pour cet override en V1.
41. Saut de versions autorisé (ex: `1.2.0` vers `3.0.0`) tant que SemVer est
    valide et unique par `model`.
42. Pré-releases SemVer (`-alpha`, `-beta`, etc.) non autorisées en V1 pour les
    releases publiées.
43. Build metadata SemVer (`+build...`) non autorisé en V1 pour les releases
    publiées.
44. La version initiale d'un `model` est libre à la création (y compris import),
    tant qu'elle respecte SemVer.
45. Chaque release suivante doit avoir une version unique et strictement
    supérieure à la précédente pour le même `model`.

## Questions ouvertes

1. Politique tags et reproductibilité historique.  
   Les releases figent la structure avec des `TagId`, mais les tags peuvent
   évoluer ou être supprimés dans `tags-core`. Il faut décider entre trois
   options: interdire la suppression d'un tag référencé par une release publiée,
   conserver les tags en suppression logique, ou autoriser la suppression
   physique en copiant les métadonnées de tags dans les snapshots de release.

2. Classification versionning des commandes existantes (`ModelRepoCmd.kt`).  
   La liste des commandes existe déjà, mais leur rôle de versionning n'est pas
   encore classé. Il faut définir quelles commandes comptent comme changement de
   contenu, quelles commandes restent non structurelles, et quelles commandes ne
   doivent pas autoriser une publication à elles seules.

3. Règles SemVer détaillées.  
   Le format SemVer et le principe de suggestion automatique sont validés, mais
   la table de correspondance n'est pas encore écrite. Il faut formaliser le
   mapping précis des commandes et des événements vers `major`, `minor` ou
   `patch`, y compris pour les cas sensibles comme renommage, changement de
   type, passage optionnel vers requis, et suppression.

4. Règles fonctionnelles de publication côté expérience utilisateur.  
   Les contraintes de cohérence techniques sont validées, mais l'expérience
   utilisateur avant confirmation n'est pas encore figée. Il faut préciser les
   informations obligatoires affichées avant publication, notamment le résumé
   des changements, la version proposée et les avertissements.

5. Parcours de comparaison entre releases.  
   L'objectif de traçabilité est validé, mais le contenu exact du diff métier
   reste à cadrer. Il faut décider le niveau de détail attendu pour les ajouts,
   suppressions, modifications et renommages, ainsi que la granularité
   d'affichage sur les objets du modèle.

6. Cycle de vie d'un `model` en fin de vie.  
   Le paradigme versionné est défini, mais les règles de suppression ou
   d'archivage ne sont pas tranchées. Il faut décider si la fin de vie passe par
   suppression physique, archivage logique, ou une combinaison, et définir
   l'impact sur `model_event`, `model_snapshot` et les releases.

7. Schéma SQL cible et plan d'initialisation V1.  
   Il n'y a pas de migration legacy à gérer, mais la structure finale reste à
   formaliser. Il faut définir les tables, les contraintes, les index et la
   séquence d'initialisation d'un nouveau modèle.

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
