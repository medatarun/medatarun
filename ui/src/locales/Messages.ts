import { type ActionPerformerViewMessages } from "./contracts/ActionPerformerViewMessages";
import { type AdminActorPageMessages } from "./contracts/AdminActorPageMessages";
import { type AdminActorsPageMessages } from "./contracts/AdminActorsPageMessages";
import { type AdminUserPageMessages } from "./contracts/AdminUserPageMessages";
import { type AdminUsersPageMessages } from "./contracts/AdminUsersPageMessages";
import { type AuthRolePageMessages } from "./contracts/AuthRolePageMessages";
import { type AuthRolesPageMessages } from "./contracts/AuthRolesPageMessages";
import { type EntityPageMessages } from "./contracts/EntityPageMessages";
import { type AttributePageMessages } from "./contracts/AttributePageMessages";
import { type ActionPageMessage } from "./contracts/ActionPageMessage";
import { type ErrorBoundaryMessages } from "./contracts/ErrorBoundaryMessages";
import { type FormValidationMessages } from "./contracts/FormValidationMessages";
import { type InlineEditRichTextLayoutMessages } from "./contracts/InlineEditRichTextLayoutMessages";
import { type InlineEditSingleLineLayoutMessages } from "./contracts/InlineEditSingleLineLayoutMessages";
import { type InlineEditTagsMessages } from "./contracts/InlineEditTagsMessages";
import { type DashboardPageMessages } from "./contracts/DashboardPageMessages";
import { type LayoutMessages } from "./contracts/LayoutMessages";
import { type LoginPageMessages } from "./contracts/LoginPageMessages";
import { type ModelPageMessages } from "./contracts/ModelPageMessages";
import { type ModelComparePageMessages } from "./contracts/ModelComparePageMessages";
import { type ModelHistoryPageMessages } from "./contracts/ModelHistoryPageMessages";
import { type ModelReportsPageMessages } from "./contracts/ModelReportsPageMessages";
import { type ModelsPageMessages } from "./contracts/ModelsPageMessages";
import { type PreferencesPageMessages } from "./contracts/PreferencesPageMessages";
import { type RelationshipDescriptionMessages } from "./contracts/RelationshipDescriptionMessages";
import { type RelationshipPageMessages } from "./contracts/RelationshipPageMessages";
import { type RelationshipsTableMessages } from "./contracts/RelationshipsTableMessages";
import { type SessionMessages } from "./contracts/SessionMessages";
import { type TagEditMessages } from "./contracts/TagEditMessages";
import { type TagGroupEditMessages } from "./contracts/TagGroupEditMessages";
import { type TagGroupsPageMessages } from "./contracts/TagGroupsPageMessages";
import { type TagsTableMessages } from "./contracts/TagsTableMessages";
import { type TypePageMessages } from "./contracts/TypePageMessages";
import { type TypesTableMessages } from "./contracts/TypesTableMessages";
import type { MenuMessages } from "@/locales/contracts/MenuMessages.ts";

export type Messages = SessionMessages &
  AttributePageMessages &
  ActionPerformerViewMessages &
  AdminActorPageMessages &
  AdminActorsPageMessages &
  AdminUserPageMessages &
  AdminUsersPageMessages &
  AuthRolePageMessages &
  AuthRolesPageMessages &
  ActionPageMessage &
  DashboardPageMessages &
  PreferencesPageMessages &
  RelationshipDescriptionMessages &
  RelationshipsTableMessages &
  ErrorBoundaryMessages &
  FormValidationMessages &
  InlineEditRichTextLayoutMessages &
  InlineEditSingleLineLayoutMessages &
  InlineEditTagsMessages &
  LayoutMessages &
  MenuMessages &
  ModelsPageMessages &
  ModelComparePageMessages &
  ModelHistoryPageMessages &
  ModelPageMessages &
  ModelReportsPageMessages &
  EntityPageMessages &
  LoginPageMessages &
  RelationshipPageMessages &
  TagGroupsPageMessages &
  TagGroupEditMessages &
  TagEditMessages &
  TagsTableMessages &
  TypePageMessages &
  TypesTableMessages;
