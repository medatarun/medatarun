import { type EntityPageMessages } from "./contracts/EntityPageMessages";
import { type ErrorBoundaryMessages } from "./contracts/ErrorBoundaryMessages";
import { type LayoutMessages } from "./contracts/LayoutMessages";
import { type LoginPageMessages } from "./contracts/LoginPageMessages";
import { type ModelPageMessages } from "./contracts/ModelPageMessages";
import { type SessionMessages } from "./contracts/SessionMessages";

export type Messages = SessionMessages &
  ErrorBoundaryMessages &
  LayoutMessages &
  ModelPageMessages &
  EntityPageMessages &
  LoginPageMessages;
