import { type EntityPageMessages } from "./contracts/EntityPageMessages";
import { type ErrorBoundaryMessages } from "./contracts/ErrorBoundaryMessages";
import { type DashboardPageMessages } from "./contracts/DashboardPageMessages";
import { type LayoutMessages } from "./contracts/LayoutMessages";
import { type LoginPageMessages } from "./contracts/LoginPageMessages";
import { type ModelPageMessages } from "./contracts/ModelPageMessages";
import { type PreferencesPageMessages } from "./contracts/PreferencesPageMessages";
import { type SessionMessages } from "./contracts/SessionMessages";

export type Messages = SessionMessages &
  DashboardPageMessages &
  PreferencesPageMessages &
  ErrorBoundaryMessages &
  LayoutMessages &
  ModelPageMessages &
  EntityPageMessages &
  LoginPageMessages;
