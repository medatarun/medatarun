# TODO `tags-core`

Ce fichier sert de mémoire de conception et de pilotage pour `tags-core`.
Il liste :

- ce qu'il reste à implémenter
- les problèmes potentiels de logique déjà visibles
- les écarts entre le `README` et l'état réel du code

Le but est de pouvoir reprendre le sujet sans dépendre du contexte oral.

## 1) Affectation de tags sur des objets métier (pas encore implémenté)

Aujourd'hui `tags-core` gère correctement :

- la définition des tags (`Tag`)
- les groupes de tags (`TagGroup`)
- les scopes (`TagScopeRef`)
- les règles métier free/managed

Mais il ne gère pas encore le fait de dire :

- "cet objet métier porte ce tag"

Il manque donc un modèle explicite d'affectation (liaison objet <-> `TagId`) et les commandes/requêtes associées.

Description de ce qu'il faudra faire :
- définir une représentation générique d'affectation de tag à un objet (objet cible + `TagId`)
- définir les commandes pour affecter / retirer un tag
- définir les requêtes pour lire les tags d'un objet (et éventuellement les objets d'un tag)
- garder `TagId` comme référence dans les affectations (pas `TagRef` par key) pour éviter les problèmes lors d'un changement de key

## 2) Règles d'acceptation des scopes par les objets (pas encore implémenté)

Le `README` décrit la règle métier cible : un objet doit définir quels scopes de tags il accepte.

Le code actuel a déjà l'infrastructure de scope (`TagScopeRef`) mais pas encore la politique d'acceptation par type d'objet.

Exemple de règle attendue (plus tard) :
- un objet de `model-core` accepte :
  - les tags managed (scope global)
  - les tags free du scope de son modèle
- il refuse les tags free d'un autre modèle

Description de ce qu'il faudra faire :
- définir un contrat de validation d'affectation (scope autorisé ou non)
- décider où vit la règle (probablement dans les modules consommateurs, pas dans `tags-core`)
- brancher cette validation dans les futures commandes d'affectation

## 3) Flux inverse manquant : "scope supprimé" -> suppression des tags du scope

Le flux `tags-core -> TagScopeManager` existe déjà :
- avant suppression d'un tag, `TagCmdsImpl` notifie les managers (`onBeforeTagDelete`)

Mais le flux inverse n'existe pas encore :
- si un scope local disparaît (ex: un `Recipe` ou un `Vehicle` supprimé dans un module consommateur), il n'existe pas encore de mécanisme standard pour demander à `tags-core` de supprimer tous les tags de ce scope

Description de ce qu'il faudra faire :
- définir une interface/port pour les événements venant des managers vers `tags-core`
- définir au moins le cas "scope supprimé"
- fournir dans `tags-core` le traitement correspondant (supprimer les tags du scope)
- définir ce qui est attendu si un des tags ne peut pas être supprimé (veto / erreur)

## 4) Problème potentiel de logique : suppression d'un `TagGroup` supprime les tags sans notifier les managers

État actuel du code :
- `TagGroupDelete` supprime le groupe en base (`TagCmdsImpl`)
- SQLite supprime les tags liés via `FOREIGN KEY ... ON DELETE CASCADE`

Problème potentiel :
- cette suppression de tags via cascade SQL ne passe pas par `TagCmdsImpl.tagManagedDelete(...)`
- donc `onBeforeDelete(tagId)` n'est pas appelé pour chaque tag supprimé
- les managers de scope ne sont pas notifiés
- des objets métier pourraient conserver des références mortes (`TagId`) vers des tags supprimés par cascade

Le test actuel `tag group delete deletes managed tags` vérifie bien la suppression des tags en storage, mais pas le nettoyage dans les objets des managers.

Description de ce qu'il faudra faire :
- décider si `TagGroupDelete` doit :
  - émettre explicitement les événements de suppression tag par tag avant de supprimer le groupe, ou
  - avoir un autre mécanisme de cleanup centralisé
- ajouter des tests métier sur ce comportement (objets taggés + suppression de groupe)

## 5) Événements lifecycle incomplets (seul `onBeforeDelete` existe)

Le `README` parle de `before` / `after`, mais le code actuel n'implémente que :
- `TagCmdsEvents.onBeforeDelete(tagId)`

Il n'existe pas encore :
- d'événement `after`
- de distinction explicite entre événements de veto et notifications post-action

Ce n'est pas forcément un bug aujourd'hui, mais c'est un point de design à compléter avant d'ajouter plus de flux inter-modules.

Description de ce qu'il faudra faire :
- définir la liste des événements `before` (veto possibles)
- définir la liste des événements `after` (notifications non veto)
- définir clairement ce que chaque événement garantit (avant mutation / après mutation)

## 6) Transactions cross-modules : stratégie non définie

Le sujet est noté dans le `README`, mais il faut garder une trace concrète ici.

État actuel :
- les managers de test sont en mémoire
- les veto `before` existent déjà

Problème potentiel futur :
- dès qu'un module consommateur persiste des données (assignations, projections, etc.), une suppression de tag peut nécessiter plusieurs écritures dans plusieurs composants
- sans stratégie de transaction, on risque des états partiels incohérents

Description de ce qu'il faudra faire :
- définir la sémantique attendue en cas d'échec (rollback strict / compensation / best effort)
- définir les frontières transactionnelles (au moins conceptuellement)
- aligner les événements `before` / `after` sur cette stratégie

## 7) Suppression en lot (ex: suppression d'un scope -> beaucoup de tags) : comportement à définir

Quand le flux "scope supprimé -> supprimer tous les tags du scope" existera, il pourra supprimer plusieurs tags d'un coup.

Problèmes à clarifier :
- suppression unitaire répétée ou commande bulk dédiée ?
- que faire si la suppression du 1er tag passe mais pas le 2e ?
- faut-il un comportement tout-ou-rien ?

Description de ce qu'il faudra faire :
- définir la sémantique de suppression multiple
- choisir si les événements sont émis par tag ou en bulk
- écrire les tests de comportement attendu

## 8) Contrat du `TagScopeManager` à préciser (responsabilités exactes)

Aujourd'hui `TagScopeManager` fait déjà deux choses :
- vérifier si un scope local existe (`localScopeExists`)
- réagir avant suppression de tag (`onBeforeTagDelete`)

C'est cohérent pour l'instant, mais le contrat va s'élargir avec le flux inverse (scope -> tags).

Description de ce qu'il faudra clarifier :
- quelles responsabilités appartiennent au manager (validation locale, veto, cleanup)
- quelles responsabilités restent dans `tags-core` (orchestration)
- comment exposer les événements remontants des managers vers `tags-core` sans couplage direct à `TagCmds`

## 9) Politique quand un plugin de scope est absent (cas runtime futur)

Le système est à extensions/plugins, et `TagScopeManager` est contribué via contribution point.

Cas possible plus tard :
- des tags existent pour un `scopeType`
- mais le plugin qui fournit le manager de ce scope n'est pas chargé / pas installé

Le code actuel sait déjà échouer proprement sur création free si le manager est absent (`TagScopeManagerNotFoundException`).
Mais d'autres comportements restent à décider.

Description de ce qu'il faudra définir :
- que se passe-t-il sur lecture de tags déjà existants pour ce scope ?
- que se passe-t-il sur suppression de tag (pas de manager pour nettoyer) ?
- faut-il bloquer certaines opérations tant que le plugin manque ?

## 10) Tests métier encore manquants autour de `TagGroupDelete` + cleanup des objets

`TagTest.kt` couvre maintenant très bien :
- create/update/delete free & managed
- unicité free par scope
- validation de scope
- veto des managers
- propagation delete (free et managed) vers objets

Mais il manque encore un test métier important :
- suppression d'un `TagGroup` avec tags managed effectivement utilisés par des objets, et vérification du cleanup (ou de l'absence de cleanup selon la décision retenue)

Description de ce qu'il faudra faire :
- écrire le test qui fixe le comportement attendu
- puis aligner l'implémentation si nécessaire

## 13) Exceptions internes `IllegalStateException` encore présentes dans quelques gardes de cohérence interne

Le module a déjà été nettoyé sur plusieurs points en remplaçant des `IllegalStateException` par des `MedatarunException` métier.

Il reste encore des `IllegalStateException` dans des gardes internes (par exemple dans `TagRef` et `TagStorageSQLite`) pour des états considérés impossibles.

Ce n'est pas nécessairement une erreur :
- certaines sont des gardes sur des états considérés impossibles en usage normal

Mais c'est un point à surveiller :
- si un de ces cas devient atteignable par des données réelles (ex: corruption / migration / appel externe), il faudra peut-être passer à une exception métier plus explicite

Description de ce qu'il faudra faire (si besoin) :
- relire ces `IllegalStateException`
- confirmer qu'elles sont bien réservées à des impossibilités internes
- sinon les remplacer par des exceptions de domaine adaptées

## 14) Écart de documentation interne : commentaire `TagRef.ByKey` obsolète

Dans `TagRef.kt`, le commentaire de `ByKey` mentionne encore les anciens formats :
- free : `"<tagKey>"`
- managed : `"<groupKey>/<tagKey>"`

Alors que le format réel implémenté est maintenant scope-aware :
- `key:global/<groupKey>/<tagKey>`
- `key:<scopeType>/<scopeId>/<tagKey>`

Ce n'est pas un bug de logique, mais c'est un piège de compréhension pour une reprise future.

Description de ce qu'il faudra faire :
- mettre à jour ce commentaire pour refléter le format réellement utilisé

## 17) Références circulaires potentielles : sujet maîtrisé pour l'instant, à surveiller avec le flux inverse

Le découplage actuel est bon :
- `TagCmdsImpl` dépend de `TagScopes` + `TagCmdsEvents`
- les implémentations utilisent la registry/médiation côté `internal`

Mais quand le flux inverse (scope managers -> tags-core) sera ajouté, il faudra préserver cette séparation pour éviter :
- un couplage direct des managers à `TagCmds`
- des boucles de dépendances/services difficiles à maintenir

Ce n'est pas un bug actuel, mais c'est un point de vigilance architectural.

Description de ce qu'il faudra faire :
- définir un port inverse explicite
- garder la médiation/orchestration hors de `TagCmdsImpl`
