import { type Messages } from "./Messages";
import { entityPageMessages } from "./fr/EntityPageMessages";
import { errorBoundaryMessages } from "./fr/ErrorBoundaryMessages";
import { layoutMessages } from "./fr/LayoutMessages";
import { loginPageMessages } from "./fr/LoginPageMessages";
import { modelPageMessages } from "./fr/ModelPageMessages";
import { sessionMessages } from "./fr/SessionMessages";

export const messages = {
  ...sessionMessages,
  ...errorBoundaryMessages,
  ...layoutMessages,
  ...modelPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
} satisfies Messages;
