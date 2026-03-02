import { type Messages } from "./Messages";
import { entityPageMessages } from "./en/EntityPageMessages";
import { errorBoundaryMessages } from "./en/ErrorBoundaryMessages";
import { layoutMessages } from "./en/LayoutMessages";
import { loginPageMessages } from "./en/LoginPageMessages";
import { modelPageMessages } from "./en/ModelPageMessages";
import { sessionMessages } from "./en/SessionMessages";

export const messages = {
  ...sessionMessages,
  ...errorBoundaryMessages,
  ...layoutMessages,
  ...modelPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
} satisfies Messages;
