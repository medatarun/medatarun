import { type CommandsPageMessages } from "../contracts/CommandsPageMessages";

export const commandsPageMessages: CommandsPageMessages = {
  commandsPage_title: "Exécuter des commandes",
  commandsPage_eyebrow: "Panneau de commandes",
  commandsPage_groupLabel: "Groupe",
  commandsPage_groupPlaceholder: "Sélectionner un groupe d'actions",
  commandsPage_actionLabel: "Action",
  commandsPage_actionPlaceholder: "Sélectionner une action",
  commandsPage_noActionSelected: "Aucune action sélectionnée",
  commandsPage_payloadLabel: "Payload",
  commandsPage_payloadPlaceholder: "Saisir un payload",
  commandsPage_noParametersRequired: "Cette action ne nécessite aucun paramètre.",
  commandsPage_submit: "Exécuter",
  commandsPage_clear: "Effacer",
  commandsPage_selectResourceAndActionError:
    "Sélectionnez une ressource et une action.",
  commandsPage_invalidPayloadError: "Payload invalide : {details}",
  commandsPage_unknownError: "erreur inconnue",
};
