import { type EntityPageMessages } from "./contracts/EntityPageMessages";
import { type LoginPageMessages } from "./contracts/LoginPageMessages";
import { type ModelPageMessages } from "./contracts/ModelPageMessages";
import { type SessionMessages } from "./contracts/SessionMessages";

export type Messages = SessionMessages &
  ModelPageMessages &
  EntityPageMessages &
  LoginPageMessages;
