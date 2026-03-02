import { type Messages } from "./Messages";
import { entityPageMessages } from "./fr/entityPage";
import { loginPageMessages } from "./fr/loginPage";
import { modelPageMessages } from "./fr/modelPage";
import { sessionMessages } from "./fr/session";

export const messages = {
  ...sessionMessages,
  ...modelPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
} satisfies Messages;
