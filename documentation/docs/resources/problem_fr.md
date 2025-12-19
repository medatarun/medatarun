---
sidebar_position: 1
---

# Le modèle de données opérationnel manquant 🇫🇷

:::note
Version française originale. Traduction disponible ici : [The Missing Operational Data Model](./problem_en.md)
:::

Cela fait plus de 30 ans que je tombe systématiquement sur le même problème.

A chaque fois que je rentre dans un projet logiciel, forcément, je pose toujours la même question : quelles sont les
données que l’on gère ? Je pose la question aux développeurs, au chef de projet, au métier, et, surprise, personne n'est
capable de me répondre :

- les métiers me montrent des écrans, donc une dérivation et une interprétation transformée et partielle des données ;
- les développeurs me disent de regarder des schémas des bases de données et des fichiers à droite à gauche, qu’on
  retrouve dans le code, perdus dans la couche métier et dans la couche persistance, donc l’implémentation ;
- le chef de projet me dit « avec un peu de chance c’est à jour sur Confluence », l’enfer ;
- quelquefois quelqu'un va me sortir un document Word écrit il y a 10 ans, qui n'aura pas survécu aux évolutions du
  logiciel.

Personne n’est capable de me donner la vision d’ensemble, les corrélations, le sens métier, les invariants, les
opérations. Je finis par devoir faire un travail d’archéologie documentaire, des entretiens à rallonge, trianguler les
informations et fouiller dans des tonnes de code, de SQL et de bases de données « live », rarement accessibles. 

Et une fois que tout est dans ma tête, on fait quoi ? On recommence avec le prochain ?

## Il suffit donc de documenter ? « Fail »

Chaque de tentative de documentation des données a toujours été infructueuse. Cela a toujours été un travail lourd,
long, qui se détache de la réalité des logiciels qui évoluent tout le temps. Personne ne veut les mettre à jour et
progressivement le projet documentaire sombre. J’ai essayé d’automatiser aussi, dans les deux sens, du métier en mode «
modèle conceptuel » vers le code avec des tonnes de transformateurs et du travail manuel, de l’introspection de code par
annotations ou du SQL vers un Wiki, rien à faire.

Pourquoi ? parce que dans la réalité, documenter n'apporte aucune valeur opérationnelle.

Autre point, on a toujours compensé cet aspect (ce manque) en se disant que ces informations étaient dans la tête des
gens, c'est la connaissance commune humaine qui fait foi.

Jusque-là, majoritairement, cela a toujours permis de s'en sortir. C'était « suffisant ».

Et parfois ça casse. Tenez, un peu de vécu :

- la seule personne qui comprenait la « gestion de la succession et de l’héritage » est partie : projet inmaintenable,
  impossible à mettre à jour d’un point de vue réglementaire, obligation d’abandon.
- ceux qui ont « dénormalisé les données » ne sont plus là pour expliquer pourquoi, au moment où on a compris c’était
  trop tard : clients bloqués à cause de problèmes de performances.
- urgence d’un client qui veut un état type RGPD de la solution sous peine de ne pas acheter : des nuits passées à tout
  fouiller.
- la gouvernance qui bloque les évolutions en Architecture Board Review faute de visibilité

J’en passe des vertes et des pas mûres

## « Rise of the Governance »

Mais le monde change.

La question de la gouvernance des données est arrivée sur le tapis, notamment à cause des réglementations (RGPD,
ISO-2701 et consors) qui s’empilent. Aujourd'hui on a besoin de savoir exactement quelles données on possède, on traite,
où elles sont sur la planète, à quel réglementation elle s'opposent, si ce sont des données sensibles, etc. Répertorier
et cataloguer toutes ces données est nécéssaire, fastidieux, improductif… chiant.

Dans la plupart des entreprises dans lesquels j'ai travaillé, cela fait avec des audits ponctuels, qui finissent en
tableaux Excel monstrueux, jamais maintenus, jamais mis à jour. Cela vient avec des lourdeurs considérables, de la
réunionite aigüee d’analyse, collecte et compréhension de toutes ces données. Autant dire que six mois après, tout le
travail qui a été fait ne sert déjà plus à rien. Même en incluant la gestion de la data dans les processus de
gouvernance (COPILs et autres ARBs), c’est voué à l'échec.

Encore une fois, tout cela n’a pas de réalité opérationnelle, juste une volonté de se conformer à des réglementations.
C'est une contrainte, pas un atout ni une valeur, mais il faut.

## « Here comes a new challenger »

Et voilà qu’un nouvel arrivant est arrivé : l’IA (et avec, les agents et assistants). Au début c'est très
impressionnant : on leur donne des schémas de base de données et un peu de code ils arrivent à peu près à s'en sortir.
C’est très joli, voir admirable, sur des bases de données modernes et bien faites…

Donnez leur maintenant vos projets « un peu legacy », ou ceux faits à la va vite — oui, celui là, avec toutes tes
colonnes en 6 caractères max, où tout est en VARCHAR dans la BDD, même les dates. Mieux, donne tes entêtes de fichiers
CSV que l’on rigole.

Très vite, on bute sur un problème: l’IA **devine** le sens des données et trop souvent, elle se trompe.

Au final, ce n’est pas pire que les humains : sans explication, l'incompréhension est la même, elle arrive juste plus
rapidement.

Et oui, car il n’y a pas de dialogue possible pour lui expliquer au fur et à mesure qu’elle découvre. L’IA n’a pas accès
à cette conscience collective du « oui, on sait », ni à ce qu'il y a dans la tête du chef de projet, ni du responsable
métier, ni du développeur… surtout quand ils ne sont plus là depuis longtemps.

Donc à un moment donné, cette documentation n'est plus juste entre « un plus », un « ça serait bien », cela devient une
**obligation**.

## Les mêmes données mais pas pareil pour tout le monde

Alors, comment on gère un système documentaire de données qui puisse servir aux métiers, aux devs, à la DSI, aux
data-analysts et aux agents IA en même temps ?

Problème encore non résolu. Analysons.

Déjà, tous les intervenants ont des optiques et des besoins différents :

- les métiers : dont le fonctionnement ne leur permet pas de parler avec les techniques, ni s'exprimer sur les données,
  ou même d’en comprendre les concepts
- les techniques : ils parlent technique, ont des besoins techniques : des codes, des tables, des types, ce qui ne parle
  absolument pas les métiers, pire, les rebute.
- la gouvernance a besoin de savoir ce qui est géré, de pouvoir sortir des rapports (ce qui est critique ou pas, où sont
  les données) pour décider
- Les chefs de projet ont besoin de savoir ce qui est à jour ou pas, savoir que si on fait évoluer les structures de
  données de manière anarchique on a des alertes, savoir que tous les écosystème évoluent en cohérence.
- Les architectes ont besoin de savoir qu’ils ne vont pas passer leurs nuits à traquer tous les projets pour détecter
  les nouvelles données sensibles ajoutées ou déplacées ou dont l’usage vient de changer. Ils ont encore moins besoin de
  devoir courir après tout le monde à chaque changement critique

Tout le monde travaille sur la même chose, mais pas de la même manière et pas avec la même vision.

On voit aussi à travers ces exemples pourquoi tout le monde freine des quatre fers à chaque modification du système et
comment les blocages politiques se mettent vitent en place.

Les artefacts que l'on a mis en place précédemment (Excel, document Word, pages Confluence, schéma de base de données
etc.) ne sont pas reliés entre eux. Un schéma de base de données, c'est pas un métier qui va le regarder. Un tableau
Excel, il sera mort au moment même où il sera sorti et il n'intéressera personne d'autre que le gouvernance.

## Un problème statique

Autre problème, ces artefacts documentaires sont purement statiques, ils ne vivent pas et il n'interagissent avec rien.
Surtout, il décrivent après coup ce que le logiciel ou le système fait déjà, pire ce que les gens pensent qu’il fait. Et
c'est pour ça qu'ils meurent. C'est pas de la négligence ou de la mauvaise volonté, c'est juste que cela ne sert à rien
dans le flux de travail réel.

À l'inverse, le code du schéma de base de données, les extractions, les ETL sont maintenus et fonctionnent, parce qu’ils
sont opérationnels. Si eux cassent c'est système entier qui casse.

## Une voie possible

De mon point de vue, l'angle mort de toute cette histoire, c'est que l'on a jamais fait du modèle conceptuel un système
vivant, interrogeable, modifiable, versionnable, intégré aux autres processus de développement avec des interfaces
graphiques adaptées, des systèmes de recherche, des systèmes de reporting et tout ce qui fait que qu’on travaille de
manière opérationnelle dessus.

Avec l'arrivée de l’IA , c’est encore plus pressant : il faut qu'on ait un système qui soit interrogé par API ou MCP, et
qui permette à l’IA de comprendre la donnée, son sens, ses invariants, ses usage, ce qu'il y a dedans. C’est là que l’on
va pouvoir générer des aides aux utilisateurs pour leur BI, à une rédaction de Stories précise, aider les devs et les
agents à coder et documenter le code, la gouvernance à interroger de manière humaine ce qu’elle possède.

Il faut que ces modèles conceptuels de données soient des objets explicites, partagés, versionnés, enrichis et surtout
utilisables directement par les humains de tous bords (business, gestion, devs, data-analyst) avec leur propre point de
vue, les IA, les automates CI/CD et toute la chaîne technique.

Ce modèle « canonique » a désormais une valeur propre :

- Consulté pour comprendre et enrichir
- Utilisé pour décider
- Exploité pour générer, valider, analyser
- et assez précis pour être consommé par une IA sans qu’elle ait à deviner : elle sait déjà

À partir du moment où le modèle existe et sert réellement à quelque chose, je suis persuadé que la dynamique s’inverse.

Le maintenir, ce n'est plus « annexe », c’est une condition de fonctionnement. Alors, on se demande plus comment on va
documenter les données, on se demande comment faire vivre un modèle commun opérationnel et partagé par tous les acteurs
du système, qu'ils soient humains ou techniques.

## Vos retours

C'est à partir de là que j'ai voulu travailler sur ce projet, pas pour faire un outil en plus parce que des outils
gouvernance de donner en a plein, mais parce que j'ai jamais trouvé de réponse satisfaisante à ce problème-là

Voilà, j'ai commencé ça : [Medatarun](https://github.com/medatarun/medatarun)

Ce qui m'intéresse avant tout aujourd'hui c'est pas de vous convaincre c'est surtout de comprendre comment ça marche
chez vous. Est-ce que vous rencontrez les mêmes problèmes ? Est-ce que ce problème de compréhension réelle des données
fait écho chez vous aujourd'hui ? Est-ce que ça vous parle ce que je suis en train de raconter ou bien est-ce que vous
voyez le problème autrement.

Je suis surtout intéressé par vos retours, d'expérience, vos désaccord, les choses que j'aurais pas vues.

Et si vous avez ce type de problème, et aucune idée de par quel bout le prendre, dites-vous que c'est exactement comment
ce projet à démarrer et je vous invite à venir en discuter avec moi.


