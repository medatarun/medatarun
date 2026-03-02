import { type EntityPageMessages } from "./contracts/EntityPageMessages";
import { type AttributePageMessages } from "./contracts/AttributePageMessages";
import { type ErrorBoundaryMessages } from "./contracts/ErrorBoundaryMessages";
import { type DashboardPageMessages } from "./contracts/DashboardPageMessages";
import { type LayoutMessages } from "./contracts/LayoutMessages";
import { type LoginPageMessages } from "./contracts/LoginPageMessages";
import { type ModelPageMessages } from "./contracts/ModelPageMessages";
import { type ModelsPageMessages } from "./contracts/ModelsPageMessages";
import { type PreferencesPageMessages } from "./contracts/PreferencesPageMessages";
import { type SessionMessages } from "./contracts/SessionMessages";
import { type TypePageMessages } from "./contracts/TypePageMessages";

export type Messages = SessionMessages &
  AttributePageMessages &
  DashboardPageMessages &
  PreferencesPageMessages &
  ErrorBoundaryMessages &
  LayoutMessages &
  ModelsPageMessages &
  ModelPageMessages &
  EntityPageMessages &
  LoginPageMessages &
  TypePageMessages;
