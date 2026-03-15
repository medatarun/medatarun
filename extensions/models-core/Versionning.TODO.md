# Versionning - Suivi d'implémentation

Ce fichier suit l'avancement du chantier `versionning/events` à partir des
décisions actées dans `Versionning.md` et `DATABASE.md`.

Le principe de travail actuel est progressif:

- le système d'events est construit par étapes
- l'implémentation actuelle continue à fonctionner en parallèle pendant ce
  chantier

Statuts:

- `[DONE]` déjà en place
- `[PARTIAL]` présent en partie
- `[TODO]` restant à implémenter

Items:

- `[VE-A][DONE]` La table `model_event` existe avec `event_type`, `event_version`, `stream_revision`, `actor_id`, `action_id`, `created_at` et `payload`.
- `[VE-B][DONE]` Le mapping `ModelStorageCmd -> event` est explicite via `@ModelEventContract`, registry dédiée et codec JSON versionné.
- `[VE-C][DONE]` L'append des events est déjà branché dans `ModelStorageDb` avec ordre canonique protégé par `UNIQUE(model_id, stream_revision)`.
- `[VE-D][DONE]` Les commandes métier continuent à mettre à jour les tables courantes en parallèle, ce qui permet de construire le système d'events sans casser l'existant.
- `[VE-E][DONE]` La lecture brute des `model_event` existe déjà via `findAllModelEvents(modelId)`.
- `[VE-F][DONE]` La couche stockage/versionning est alignée sur le vocabulaire `release`: `ModelStorageCmd.ModelRelease` sérialise l'event `model_release`.
- `[VE-G][PARTIAL]` La validation `ModelVersion` actuelle accepte encore les suffixes SemVer `-pre-release` et `+build`, alors que la V1 de `Versionning.md` impose `MAJOR.MINOR.PATCH` uniquement.
- `[VE-H][DONE]` Les tests de contrat JSON figés des events existent déjà: vérification de `event_type`, `event_version`, payload JSON figé et round-trip encode/decode.
- `[VE-L][TODO]` Ajouter les tables SQL de projection `model_snapshot` et associées, sans retirer les tables courantes tant que la projection n'est pas stabilisée.
- `[VE-M][TODO]` Introduire `snapshot_kind = CURRENT_HEAD | VERSION_SNAPSHOT` et les contraintes SQL minimales associées.
- `[VE-I][DONE]` Le vrai event de stockage `model_release` existe, distinct des updates courants, avec version portée dans l'event.
- `[VE-J][DONE]` `model_version` dans `model_event` est rempli pour les events `model_release`.
- `[VE-K][TODO]` Ajouter les premières règles métier de publication d'une release: version obligatoire, unicité par modèle, refus sans changement depuis la release précédente.
- `[VE-O][TODO]` Implémenter un projecteur transactionnel `model_event -> CURRENT_HEAD` construit en parallèle de l'écriture actuelle.
- `[VE-P][TODO]` Créer atomiquement le `VERSION_SNAPSHOT` lors d'une `release`, à partir du `CURRENT_HEAD`.
- `[VE-Q][TODO]` Ajouter le replay complet depuis `model_event` pour reconstruire un modèle et vérifier la cohérence avec le `CURRENT_HEAD`.
- `[VE-R][TODO]` Refaire `create` et `import` pour produire leur séquence initiale d'events puis la `release` initiale dans une seule transaction.
- `[VE-S][TODO]` Ajouter les requêtes de lecture du versionning: liste des releases, chargement d'une version précise, historique et diff entre releases.
- `[VE-T][TODO]` Basculer progressivement les lectures rapides vers `model_snapshot` quand la projection sera validée.
- `[VE-U][TODO]` Décider la politique historique des tags référencés par les releases publiées.
