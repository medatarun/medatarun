import { type Messages } from "./Messages";
import { entityPageMessages } from "./en/entityPage";
import { loginPageMessages } from "./en/loginPage";
import { modelPageMessages } from "./en/modelPage";
import { sessionMessages } from "./en/session";

export const messages = {
  ...sessionMessages,
  ...modelPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
} satisfies Messages;
