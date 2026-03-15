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

Items PARTIAL et TODO:

- `[VE-L][TODO]` Poser directement le schéma SQL final du versionning: `model` réduit à son rôle d'identité stable, tables `model_snapshot` et `*_snapshot`, tables de tags snapshot et tables de recherche snapshot.
- `[VE-M][TODO]` Introduire dans ce schéma final `snapshot_kind = CURRENT_HEAD | VERSION_SNAPSHOT`, `up_to_revision`, `model_event_release_id`, `version`, `lineage_id` et les contraintes SQL minimales associées, en gardant `model_event` comme seule source de vérité de version.
- `[VE-N][TODO]` Adapter l'écriture actuelle pour alimenter temporairement le schéma final `CURRENT_HEAD` sans attendre le replay complet depuis `model_event`.
- `[VE-O][TODO]` Remplacer cette alimentation directe temporaire par un projecteur transactionnel `model_event -> CURRENT_HEAD`.
- `[VE-K][TODO]` Ajouter les premières règles métier de publication d'une release: version obligatoire, unicité par modèle, refus sans changement depuis la release précédente.
- `[VE-P][TODO]` Créer atomiquement le `VERSION_SNAPSHOT` lors d'une `release`, à partir du `CURRENT_HEAD`.
- `[VE-Q][TODO]` Ajouter le replay complet depuis `model_event` pour reconstruire un modèle et vérifier la cohérence avec le `CURRENT_HEAD`.
- `[VE-R][TODO]` Refaire `create` et `import` pour produire leur séquence initiale d'events puis la `release` initiale dans une seule transaction.
- `[VE-S][TODO]` Ajouter les requêtes de lecture du versionning: liste des releases, chargement d'une version précise, historique et diff entre releases.
- `[VE-T][TODO]` Basculer les lectures rapides sur `CURRENT_HEAD` et les snapshots de version, puis retirer l'ancienne alimentation directe des tables courantes qui n'ont plus de rôle dans la cible finale.
- `[VE-U][TODO]` La décision sur la politique historique des tags est actée dans `Versionning.md`: les releases conservent les `TagId` attachés, pas les métadonnées complètes des tags. L'implémentation SQL et applicative de cette règle reste à faire dans les snapshots et les lectures historiques.
