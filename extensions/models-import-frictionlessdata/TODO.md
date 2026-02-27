# TODO `models-import-frictionlessdata`

Ce fichier liste uniquement le travail restant et les écarts entre le contrat
cible du module (décrit dans `README.md`) et l'état actuel du code.

Ce qui est implémenté doit rester dans le README.
Ce qui ne l'est pas encore doit être suivi ici, puis supprimé une fois livré.

## 1) Import tags Frictionless : clarifications restantes

Décision déjà actée:
- les `keywords` rencontrés pendant l'import sont créés comme tags locaux au
  modèle importé (`model/<modelId>`) et leurs `TagId` sont injectés dans le
  modèle importé
- ordre du flux:
  - 1/ résoudre ou créer les tags dans le scope du futur `ModelId`
  - 2/ créer le modèle avec les `TagId` déjà attachés

Points restant à trancher:
- politique `keyword` -> `TagKey`:
  - aujourd'hui: `trim` + suppression des doublons
  - à décider: normalisation stricte, rejet, mapping, reporting
- validation du scope local `model/<modelId>` pendant l'import:
  - l'import crée les tags avant persistance du modèle
  - unifier/encadrer la coexistence entre validation stricte des scopes `model`
    et besoin de précréation lors de l'import

## 2) Couverture de tests à compléter

- tests de création effective des tags via `TagCmds`
- tests de déduplication des keywords
- tests de propagation des `TagId` dans `Model.tags` et `Entity.tags`
- tests des keywords incompatibles avec `TagKey` (règle à définir)
