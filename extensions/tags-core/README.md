# tags-core

## Note pour IA (style de rédaction attendu)

Quand on rédige ce README:

- écrire des faits concrets et vérifiables
- dire explicitement qui fait quoi, où, quand, avec quelles règles
- nommer directement les problèmes et les effets opérationnels ou logiques
- éviter le style blog, le ton marketing, les généralités et les formulations
  vagues
- éviter l'enrobage; préférer des phrases courtes, directes, actionnables
- poser des questions sur les éléments qui paraissent flous avant de rédiger

## Convention README / TODO

Ce README décrit le contrat fonctionnel cible du module (comportement attendu
une fois le lot finalisé).

Le fichier `TODO.md` liste uniquement le travail restant et les écarts entre ce
contrat cible et l'état actuel du code.

## But du module

`tags-core` est le module transversal qui centralise la gestion des tags dans
Medatarun (définition, lifecycle, scope de tag global, etc.).

Les modules consommateurs (ex: `models-core`) gèrent l'attache/détache sur leurs
objets et stockent des `TagId` dans les objets taggés et définissent leur scope
de tags.

Le module `tags-core` est utilisé aujourd'hui par le
module [models-core](../models-core/README.md).
Roadmap (lot ultérieur, non implémenté, décision actée) : intégration à
`prompts-core` et aux autres modules métiers.

Le module `tags-core` expose:

- un modèle unique (`Tag`, `TagId`, `TagRef`, `TagScopeRef`)
- des commandes de gestion de tags/groupes (via `TagAction`, `TagCmds` et
  `TagQueries`)
- outils communs que les autres extensions peuvent utiliser pour travailler avec
  les tags

## Problème initial

On constate deux modes de gestion des tags en général dans les applications

- Modèle global/centralisé:
    - Par exemple : OpenMetadata, DataHub, Atlan, Alation, Collibra
    - Effet: création/modification plus contrôlée, cycle de changement plus
      long, une équipe locale ne peut pas créer/ajuster vite un tag pour un
      besoin immédiat
- Modèle local/flexible:
    - Par exemple : Jira labels
    - Effet: création rapide, vocabulaire hétérogène (doublons, synonymes,
      homonymes), deux équipes taggent le même sujet différemment, donc la
      recherche transversale rate des résultats ou renvoie du bruit

- Dans plusieurs outils, ce sens n'est pas porté directement par le tag utilisé:
    - Jira labels: pas de description métier attachée au label
    - dbt tags: liste de chaînes sans champ natif de description sur le tag
      lui-même
    - OpenLineage tag facets: paires clef/valeur/source, sans champ de
      description de la signification du tag
    - CKAN free tags: tags libres orientés mot-clef, sans description métier
      directement attachée au tag
    - outils de catalogage avec glossaire séparé (DataHub, Atlan, Alation,
      Collibra, OpenMetadata): le sens est souvent porté par un objet de
      glossaire/terme distinct, pas par le tag appliqué lui-même

Sans description de tag, l'utilisateur ne sait pas:

- quel tag choisir
- sur quels objets il doit l'appliquer
- quand il doit l'appliquer
- quand il ne doit pas l'appliquer

Résultat:

- tags appliqués au hasard selon les personnes
- mêmes objets taggés différemment
- recherche par tags non fiable
- règles de gouvernance non applicables de manière consistante

## Résumé métier

Le système distingue deux usages métier de tags: les tags à portée globale et
les tags locaux. Les tags peuvent être documentés à l'aide de leur nom et leur
description. Des mécaniques de codification permettent d'attribuer des
identifiants stables pour l'automatisation.

### Tags globaux

> Version courte: on peut créer des tags globaux qu'on applique partout, mais il
> faut avoir des droits particuliers et ils doivent être classés dans des
> groupes.

Un tag de portée globale peut être appliqué sur tous les éléments du système qui
supportent la notion de tag. Il est déclaré au niveau système, dans un groupe
de tags. Pour gérer ces tags, des permissions spécifiques aux tags globaux sont
disponibles.
Ces tags globaux sont typiquement utilisés pour les usages transversaux (
gouvernance forte).

Les tags globaux sont organisés par groupes.

Le but des groupes de tags est de structurer le vocabulaire par thème métier (
securité, privacy, rétention, etc.). Cela permet ainsi de porter le contexte de
gouvernance d'un ensemble de tags et de définir leur périmètre et leur unicité
dans chaque groupe. Des permissions spécifiques à leur gestion s'appliquent.

Les tags globaux ne peuvent être créés que dans le cadre d'un groupe de tags.
Un tag global ne peut être que dans un seul groupe de tags à la fois.
Les tags du groupe sont supprimés si le groupe est supprimé.

Exemples typiques d'usages:

- classifications RGPD / privacy
- classifications security
- labels de localisation / internationalisation
- labels de gouvernance par application propriétaire

Exemple:

- groupe `security`: `public`, `internal`, `restricted`
- groupe `retention`: `30d`, `365d`, `7y`

### Tag locaux

> Version courte: on peut créer des tags dans des modèles sans avoir besoin de
> permissions particulières ormis celles de pouvoir gérer le modèle. Cela sera
> étendu à d'autres concepts que les modèles plus tard.

Un tag de portée locale peut être créé au niveau de certains objets du système
destinés à contenir des définitions de tags.

On appelle _scope_ d'un tag local ce périmètre, c'est-à-dire l'endroit où il est
déclaré et la portée sur lequel il s'applique.

Par exemple, dans l'application, on gère des _modeles_. Quand un tag est créé
dans un modèle, il peut s'appliquer à tous les éléments taggables du modèle (
entités, relations, attributs, etc.). Le _scope_ est donc **ce** modèle. Le tag
ne pourra pas être appliqué dans un autre modèle que celui-là.

Les permissions sont liées à l'objet dans lequel ils sont créés. Ils
disparaissent quand l'objet dans lequel ils sont créés disparait.

Les tags locaux ne peuvent pas être inclus dans les groupes de tags globaux.

Exemples typique: données affichées à l'écran ou pas, celles qui sont
utilisées pour la recherche, conventions, qualité de code ou de data, etc.

### Tags et groupes documentés

Les groupes de tags, les tags locaux et globaux ont tous un nom et une
description facultative. Le but est de permettre aux utilisateurs de documenter
leur usage, leur sens et leurs règles d'application, sans toutefois les obliger.

### Codification

Les tags locaux et globaux, ainsi que les groupes disposent tous d'une clef
métier, appellée `key`. La `key` est assez stricte : lettres, chiffres, tirets,
soulignés uniquement. Pas de caractères non ASCII.

Cela permet d'avoir un code métier stable, permettant au nom du tag ou du
groupe de changer, aux scripts et API d'avoir un point stable et plus simple
que les identifiants.

Avec les règles d'unicité suivantes :

- deux tags locaux d'une même scope ne peuvent pas avoir la même `key`
- deux tags globaux d'une même groupe ne peuvent pas avoir la même `key`
- deux groupes de tags ne peuvent pas avoir la meme `key`
- le reste est possible (même `key` dans deux scopes différents, entre deux
  groupes, entre un local et un global, etc.)

Une `key` peut être modifiée. Cela n'impacte pas l'identité du tag (son id) et
ce qui y fait référence dans le système.

Neanmoins si des systèmes automatisés s'en servent (API, scripts) c'est à
l'utilisateur de faire ce changement en conscience et le système n'a pas
vocation à l'en empêcher.

### Espaces d'identifiants et addressage

Les tags locaux et globaux partagent le même objet `Tag` et le même espace
d'identifiants ( `TagId`). La différence est portée par les règles métier et les
commandes, pas par deux modèles techniques séparés.

`TagId` est donc l'identifiant stable d'un tag. C'est la référence de stockage
dans les
objets métiers. En pratique, c'est un UUID v7.

On peut référencer un Tag via API ou CLI grâce à `TagRef`. C'est la référence d'
entrée/sortie API (action, JSON, recherche), sous forme:

- soit une clef de type `id:<TagId>`, on appelle cette forme un `TagRef.ById`
- soit une clef de type `key:<scopeType>/<groupeKey|scopeId>/<TagKey>`, on
  appelle cette forme un `TagRef.ByKey`

Règle de format pour `TagRef.ByKey`

- si `scopeType = global`, le segment du milieu est un `groupKey`
- sinon, le segment du milieu est `scopeId`

Le fait de pouvoir référencer les tags par `TagRef` avec des keys stables
simplifie l'écriture des scripts d'automatisation en évitant les boucles de
type "liste + récupération de l'id pour pouvoir agir".

## Autres usages possibles

- le fait d'avoir des tags globaux fortement gouvernés et locaux permet deux
  vitesses d'usage différentes. Typiquement, on ne bloque pas les équipes sur
  leur gestion des modèles parce qu'un tag n'existe pas : ils le créent eux même
  pour leurs besoins locaux. A l'inverse les tags globaux sont plus fortement
  réglementés, évitant ainsi la prolifération anarchique de tags.
- CLI et API peuvent se servir du système de tags comme base pour créer de la
  documentation ou alimenter des systèmes tiers.
- MCP/agents IA: ils peuvent tagger ou comprendre le sens des tags créés et
  ceux ajoutés aux objets métiers sans qu'elles aient à inférer.
  Conséquence directe : on peut demander à une IA de tagger automatiquement
  des modèles, ou l'inverse, prompter une IA sur les modèles taggés avec les
  tags qui lui fournissent des compléments d'explication.
- Sur l'UI on peut aider les utilisateurs à tagger ou à
  parcourir les tags existants pour qu'ils en comprennent le sens.

## TagScopeRef

Dans l'application (le code et les API), on a besoin de dire que l'on parle d'un
scope local ou du périmètre global.

La notion de `TagScopeRef` existe pour combler cela.

On a donc deux variantes de `TagScopeRef`

- `TagScopeRef.Global`: pas de paramètres, c'est le global
- `TagScopeRef.Local(type, id)`: on fait référence à un scope local, donc il
  faut donner le nom du scope et l'identifiant de l'objet qui a créé le scope.

Exemples en Json / API / CLI:

```json lines
// scope ref global
{
  type: "global"
}
// scope ref pour adresser les tags d'un modèle
{
  type: "model",
  id: "0000-0000-0000"
}
```

C'est utile notamment pour créer un tag local:

```json5
{
  scopeRef: {
    type: "model",
    id: "0000-0001"
  },
  key: "pii",
  name: "Personal Information",
  description: "Use on data that represent personal information as in GPRD"
}
```

## Fonctionnement des actions `tags-core`

Les actions dans `TagAction` sont l'exposition publique de `tags-core` aux
utilisateurs finaux. Les actions sont dispatchées par `TagActionProvider` à
l'API interne, c'est-à-dire `TagCmds` et `TagQueries`.

Entrées principales exposées côté actions (`TagAction`):

- tag group: create/update/delete
- local tag: create/update/delete
- global tag: create/update/delete
- liste et recherche de tags avec filtres: `TagSearch`
- liste des groupes: `TagGroupList`

Règles métier actuellement codées (`TagCmdsImpl`):

- `TagLocalCreate` refuse un scope global.
- `TagLocalCreate` exige un scope local existant (validation via
  `TagScopeManager.localScopeExists`).
- Un tag local est unique par `(scope local, key)`.
- Un tag global est unique par `(group, key)`.
- Les commandes sur les tags locaux refusent les refs vers global (
  `TagLocalCommandIncompatibleTagRefException`).
- Les commandes global refusent les refs vers local (
  `TagGlobalCommandIncompatibleTagRefException`).
- Toute mutation tag passe par `DbTransactionManager.runInTransaction`.

## Scopes et cycle de vie

`tags-core` possède un registre de scopes (`TagScopeManager`) alimenté par les
autres modules.

Exemple déjà branché:

- `models-core` déclare un `TagScopeManager` de type `model`.
- `localScopeExists` vérifie l'existence réelle du model via `ModelQueries`.

Suppression:

1. Suppression explicite de tag (local/global)

- `TagBeforeDeleteEvt(tagId)` est émis avant suppression
- Permet aux modules consommateurs de nettoyer les références ou bloquer la
  suppression

2. Suppression de groupe de tags

- Supprime tous les tags du groupe
- Émet `TagBeforeDeleteEvt` pour chaque tag supprimé

3. Suppression de scope local

- Le module propriétaire du scope emet un évenement
  `TagScopeBeforeDeleteEvent(scopeRef)`
- `tags-core` supprime en masse les tags du scope (`TagScopeDelete`)
- Décision actée: ce chemin n'émet pas d'événement `TagBeforeDeleteEvt` pour
  chaque tag supprimé

## Sécurité

Rôles enregistrés par `tags-core`:

- `tag_local_manage`
- `tag_global_manage`
- `tag_group_manage`

Règles actions:

- local tag actions -> `TAG_LOCAL_MANAGE`
- global tag actions -> `TAG_GLOBAL_MANAGE`
- group actions -> `TAG_GROUP_MANAGE`
- recherche/liste (`tag_search`, `tag_group_list`) -> utilisateur connecté (
  `SIGNED_IN`)

Permissions des tags locaux:

- état actuel: contrôle uniquement via le rôle global `tag_local_manage`
- lot ultérieur (décision actée): lors des opérations local
  create/update/delete,
  le contrôle du rôle `tag_local_manage` reste obligatoire, puis des événements
  `onBeforeTagLocalCreate` et `onBeforeTagLocalUpdate` exposent le `TagRef`
  concerné pour que le module propriétaire du scope puisse refuser l'opération
  selon ses permissions locales
- le même mécanisme de veto devra être appliqué aux autres types de scopes

## Fonctionnement UI

Depuis la mise en place de `tags-core`, l'UI n'est pas encore alignée avec le
nouveau système de tags structuré.

Règle UI à ne pas casser:

- toutes les communications UI -> backend utilisent `TagRef.ById`
  (`id:<TagId>`) et jamais `TagRef.ByKey`

## Usage direct de `tags-core`

Actions et requêtes exposées par `TagAction`:

- commandes local: `TagLocalCreate`, `TagLocalUpdateName`,
  `TagLocalUpdateDescription`, `TagLocalUpdateKey`, `TagLocalDelete`
- commandes tag group: `TagGroupCreate`, `TagGroupUpdateName`,
  `TagGroupUpdateDescription`, `TagGroupUpdateKey`, `TagGroupDelete`
- commandes global tag: `TagGlobalCreate`, `TagGlobalUpdateName`,
  `TagGlobalUpdateDescription`, `TagGlobalUpdateKey`, `TagGlobalDelete`
- requêtes: `TagSearch`, `TagGroupList`

## Usage dans les autres modules (focus `models-core`)

`models-core` consomme `tags-core` sur deux axes:

- pour ajouter / enlever des tags sur les objets de models-core
- pour réagir aux évènements émis par `tags-core` (suppression de tags par
  exemple)

Le détail est expliqué dans [models-core](../models-core/README.md)

## Décisions prises dans `tags-core`

1. Modèle tag unifié

- un seul type `Tag` pour `local` + `global`
- différence pilotée par `scope` et règles de commande

2. Référence unifiée

- `TagRef` accepte ID ou clef portée (`scope` explicite)

3. Politique d'unicité au niveau métier

- contrôlée dans le code (`TagCmdsImpl`)
- pas de contrainte SQL unique transverse entre local et global
- cohérence entre `Tag.scope` et `Tag.groupId` contrôlée au niveau métier
  (commandes/règles), pas par contrainte SQL
- règle de cohérence:
    - `scope = Global` => `groupId` requis
    - `scope = Local` => `groupId` absent

4. Validation de scope locale déléguée

- chaque module propriétaire fournit un `TagScopeManager`
- `tags-core` ne connaît pas la logique d'existence des objets externes

5. Séparation suppression tag vs suppression scope

- événements et responsabilités séparés pour éviter les ambiguïtés

## Contrat pratique pour les modules consommateurs

Contrat requis pour un module consommateur:

- déclarer un `TagScopeManager` s'il introduit un scope local
- utiliser `TagRef` dans ses actions/DTO/API d'entrée
- résoudre vers `TagId` avant persistance de ses objets
- stocker `TagId` (pas des clés de tags) pour ne pas casser les liens lors de
  renommage de key
- écouter `TagBeforeDeleteEvt` si le module stocke des références de tags ET
  doit réagir à une suppression explicite de tag/groupe (typiquement nettoyer
  les tags déjà placés sur ses objets)
- suppression de scope local (quand un Modele est supprimé par exemple), le
  module doit supprimer tout usage des tags dans le scope à supprimer (modèle,
  entités, relations, etc.). Ensuite seulement il émet [
  `TagScopeBeforeDeleteEvent`](./src/main/kotlin/io/medatarun/tags/core/domain/TagScopeBeforeDeleteEvent.kt)
  qui dira à `tags-core` de supprimer tous les tags du scope.

Stratégie d'import des tags:

- ici, "import" désigne les modules qui importent des données externes
  (exemples: frictionless, jdbc, etc.)
- pour les modules d'import (`frictionless`, `models`, `jdbc`, etc.), la
  stratégie de résolution/création des tags est à la discrétion du module
  importeur
- `tags-core` fournit le contrat de tags (scope, `TagRef`, commandes, règles de
  cohérence) mais n'impose pas une stratégie d'import homogène entre modules

## Références source

-
`extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/actions/TagAction.kt`
-
`extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/internal/TagCmdsImpl.kt`
- `extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagRef.kt`
-
`extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagScopeRef.kt`
-
`extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagBeforeDeleteEvt.kt`
-
`extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagScopeBeforeDeleteEvent.kt`
-
`extensions/models-core/src/main/kotlin/io/medatarun/model/actions/ModelAction.kt`
-
`extensions/models-core/src/main/kotlin/io/medatarun/model/internal/ModelCmdsImpl.kt`
-
`extensions/models-core/src/main/kotlin/io/medatarun/model/ModelTagResolverWithQueries.kt`
- `extensions/models-core/src/main/kotlin/io/medatarun/model/ModelExtension.kt`
- `extensions/tags-core/TAGS_UI.md`
- `extensions/tags-core/TODO.md`
