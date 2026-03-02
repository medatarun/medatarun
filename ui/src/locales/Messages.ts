import { type ActionPerformerViewMessages } from "./contracts/ActionPerformerViewMessages";
import { type EntityPageMessages } from "./contracts/EntityPageMessages";
import { type AttributePageMessages } from "./contracts/AttributePageMessages";
import { type CommandsPageMessages } from "./contracts/CommandsPageMessages";
import { type ErrorBoundaryMessages } from "./contracts/ErrorBoundaryMessages";
import { type InlineEditRichTextLayoutMessages } from "./contracts/InlineEditRichTextLayoutMessages";
import { type InlineEditSingleLineLayoutMessages } from "./contracts/InlineEditSingleLineLayoutMessages";
import { type InlineEditTagsMessages } from "./contracts/InlineEditTagsMessages";
import { type DashboardPageMessages } from "./contracts/DashboardPageMessages";
import { type LayoutMessages } from "./contracts/LayoutMessages";
import { type LoginPageMessages } from "./contracts/LoginPageMessages";
import { type ModelPageMessages } from "./contracts/ModelPageMessages";
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

export type Messages = SessionMessages &
  AttributePageMessages &
  ActionPerformerViewMessages &
  CommandsPageMessages &
  DashboardPageMessages &
  PreferencesPageMessages &
  RelationshipDescriptionMessages &
  RelationshipsTableMessages &
  ErrorBoundaryMessages &
  InlineEditRichTextLayoutMessages &
  InlineEditSingleLineLayoutMessages &
  InlineEditTagsMessages &
  LayoutMessages &
  ModelsPageMessages &
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
