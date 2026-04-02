# Plan de tests base de données (SQLite + PostgreSQL)

## But

Pouvoir lancer les tests avec :

- SQLite (comportement actuel)
- PostgreSQL

avec **le même code de tests**.

## 1. Ajouter les options et tâches Gradle

Ajouter une propriété Gradle :

- `-PdbEngine=sqlite|postgresql`
- valeur par défaut : `sqlite`

Ajouter deux tâches explicites :

- `testSqlite` : lance les tests avec `dbEngine=sqlite`
- `testPostgresql` : lance les tests avec `dbEngine=postgresql`

Les tâches de test lisent `dbEngine` via `systemProperty`.

## 2. Vérifier les paramètres PostgreSQL dans Gradle

La vérification se fait dans Gradle avant exécution des tests PostgreSQL.
Gradle ne crée pas de schéma et ne fait pas le cleanup PostgreSQL dans cette étape.

Le plan ne force pas un canal unique (env vars, `-P`, ou autre). On réutilise les mêmes clés de config Medatarun.

Minimum requis pour `testPostgresql` :

- URL PostgreSQL
- user
- password

Si un paramètre requis manque : échec immédiat avec message explicite.

## 3. Centraliser la configuration DB de test

Créer un helper de test unique (ex: `TestDbConfig`) qui construit la map `props` passée à `MedatarunConfig.createTempConfig(...)`.

Cas SQLite :

- URL SQLite mémoire aléatoire (comme aujourd'hui)

Cas PostgreSQL :

- `medatarun.storage.datasource.jdbc.dbengine=postgresql`
- `medatarun.storage.datasource.jdbc.url=<postgres-url>`
- `medatarun.storage.datasource.jdbc.properties.user=<user>`
- `medatarun.storage.datasource.jdbc.properties.password=<password>`
- `medatarun.storage.datasource.jdbc.properties.currentSchema=<schema>`

## 4. Migrer une fixture pilote

Commencer par une seule fixture (ex: `AuthEnvTest`) pour valider l'approche de bout en bout avant de généraliser.

Critères de validation :

- exécution SQLite inchangée
- exécution PostgreSQL fonctionnelle
- pas de changement de comportement métier des tests

## 5. Isoler PostgreSQL par test

L'isolation PostgreSQL est gérée dans le lifecycle JUnit, pas dans Gradle.
Objectif: ne pas annoter tous les tests et ne pas distinguer manuellement tests DB / non-DB.

Composants :

- un registre singleton de test (ex: `PostgresTestSchemaRegistry`)
- une extension JUnit 5 globale auto-enregistrée
- le helper `TestDbConfig` qui déclare chaque schéma créé dans le registre

Flux :

1. `BeforeEach` (extension): crée un `testId` courant.
2. Quand une fixture DB crée la plateforme en mode PostgreSQL:
   - création d'un schéma unique (ex: `test_<module>_<uuid>`)
   - injection `currentSchema=<schema>`
   - enregistrement `(testId, schema)` dans le registre.
3. `AfterEach` (extension):
   - récupère tous les schémas du `testId`
   - exécute `DROP SCHEMA <schema> CASCADE` pour chacun
   - vide l'entrée du registre.

Le registre stocke une liste de schémas par `testId` (un test peut créer plusieurs plateformes DB).

## 6. Migrer les autres environnements de test vers le helper

Remplacer les maps hardcodées SQLite dans les env de tests.

Cibles minimales :

- `libs/platform-auth/.../AuthEnvTest`
- `extensions/tags-core/.../TagTestEnv`
- `extensions/models-core/.../ModelTestEnv`
- `extensions/platform-actions-storage-db/.../ActionAuditDbTestEnv`
- `app/.../AppRuntimeTest`

## 7. Lancer la validation complète

Exécuter au minimum :

- `testSqlite`
- `testPostgresql`

Critère de fin :

- les deux tâches passent sur la suite ciblée.
