# Versionning - Suivi d'implémentation

Ce fichier suit l'avancement du chantier `versionning/events` à partir des
décisions actées dans `Versionning.md` et `DATABASE.md`.

Le principe de travail actuel est progressif:

- le système d'events est construit par étapes
- le schéma SQL cible est posé directement, sans introduire de schéma
  intermédiaire destiné à être remplacé plus tard
- l'alimentation actuelle est adaptée temporairement pour écrire dans ce schéma
  final tant que le projecteur complet `model_event -> CURRENT_HEAD` n'est pas
  en place

Statuts:

- `[DONE]` déjà en place
- `[PARTIAL]` présent en partie
- `[TODO]` restant à implémenter

Items DONE:

- `[VE-A][DONE]` La table `model_event` existe avec `event_type`, `event_version`, `stream_revision`, `actor_id`, `action_id`, `created_at` et `payload`.
- `[VE-B][DONE]` Le mapping `ModelStorageCmd -> event` est explicite via `@ModelEventContract`, registry dédiée et codec JSON versionné.
- `[VE-C][DONE]` L'append des events est déjà branché dans `ModelStorageDb` avec ordre canonique protégé par `UNIQUE(model_id, stream_revision)`.
- `[VE-D][DONE]` Les commandes métier continuent à écrire un état courant en parallèle du log d'events; ce branchement temporaire sert de point d'appui pour migrer ensuite vers l'alimentation du schéma final.
- `[VE-E][DONE]` La lecture brute des `model_event` existe déjà via `findAllModelEvents(modelId)`.
- `[VE-F][DONE]` La couche stockage/versionning est alignée sur le vocabulaire `release`: `ModelStorageCmd.ModelRelease` sérialise l'event `model_release`.
- `[VE-G][DONE]` Le cadrage documentation/versionning est aligné sur `ModelVersion`: les versions acceptées et leur ordre sont ceux définis par `ModelVersion`.
- `[VE-H][DONE]` Les tests de contrat JSON figés des events existent déjà: vérification de `event_type`, `event_version`, payload JSON figé et round-trip encode/decode.
- `[VE-I][DONE]` Le vrai event de stockage `model_release` existe, distinct des updates courants, avec version portée dans l'event.
- `[VE-J][DONE]` `model_version` dans `model_event` est rempli pour les events `model_release`.
- `[VE-L][DONE]` Le schéma SQL final du versionning est posé dans `v000`: `model` réduit à son rôle d'identité stable, tables `model_snapshot` et `*_snapshot`, tables de tags snapshot et tables de recherche snapshot.
- `[VE-M][DONE]` `snapshot_kind = CURRENT_HEAD | VERSION_SNAPSHOT`, `up_to_revision`, `model_event_release_id`, `version`, `lineage_id` et les contraintes SQL minimales principales sont en place.
- `[VE-N][DONE]` L'écriture actuelle alimente temporairement le schéma final `CURRENT_HEAD` sans attendre le replay complet depuis `model_event`.
- `[VE-P][DONE]` La `release` crée un `VERSION_SNAPSHOT` à partir du `CURRENT_HEAD` et le rattache à l'event `model_release`.
- `[VE-R][DONE]` `create` et `import` produisent leur séquence initiale puis la `release` initiale dans la même transaction applicative.
- `[VE-Y][DONE]` Le stockage et les contraintes de `model_snapshot.version` sont alignés avec la règle actée: valeur présente sur `VERSION_SNAPSHOT` et sur `CURRENT_HEAD`, avec le sens "dernier numéro publié connu" pour `CURRENT_HEAD`.

Items PARTIAL et TODO:

- `[VE-K1][TODO]` Refuser une release si la version demandée existe déjà pour le même modèle.
- `[VE-K2][TODO]` Refuser une release si la version demandée n'est pas strictement supérieure à la dernière release du modèle.
- `[VE-K3][TODO]` Refuser une release s'il n'y a eu aucun changement depuis la release précédente.
- `[VE-O][TODO]` Remplacer l'alimentation directe actuelle par un projecteur transactionnel `model_event -> CURRENT_HEAD`.
- `[VE-Q][TODO]` Ajouter un replay complet depuis `model_event` pour reconstruire un modèle et vérifier la cohérence avec le `CURRENT_HEAD`.
- `[VE-K4][TODO]` Refuser une release si la reconstruction `model_event -> CURRENT_HEAD` échoue.
- `[VE-S][TODO]` Ajouter les lectures versionnées dans `ModelQueries`: liste des releases, chargement d'une version précise, historique des releases.
- `[VE-U][TODO]` Ajouter la restitution historique des tags dans les lectures versionnées.
- `[VE-W][TODO]` Ajouter les tests bout en bout du versionning après ajout des lectures versionnées dans `ModelQueries`.
- `[VE-X][TODO]` Après ajout des lectures versionnées dans `ModelQueries`, compléter les tests métier existants hors versionning pour vérifier aussi le comportement versionné.
- `[VE-V][TODO]` Ajouter des tests dédiés sur `CURRENT_HEAD.up_to_revision` quand la forme finale de projection sera stabilisée.
