import { type AdminUsersPageMessages } from "../contracts/AdminUsersPageMessages";

export const adminUsersPageMessages: AdminUsersPageMessages = {
  adminUsersPage_actions: "Actions",
  adminUsersPage_eyebrow: "Administration",
  adminUsersPage_title: "Utilisateurs",
  adminUsersPage_description:
    "Medatarun gère sa sécurité et les rôles par acteur. Un acteur peut provenir de Medatarun (un compte utilisateur sur cette page) ou de services externes comme Microsoft Azure, Google ou Auth0.",
  adminUsersPage_roleReminder:
    "Après avoir créé un compte ici, affectez-lui un rôle dans son acteur correspondant. Sans rôle, l'utilisateur ne pourra pas utiliser l'application.",
  adminUsersPage_empty: "Aucun utilisateur trouvé",
  adminUsersPage_disabled: "désactivé",
};
