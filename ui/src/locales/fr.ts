import { type Messages } from "./Messages";
import { dashboardPageMessages } from "./fr/DashboardPageMessages";
import { entityPageMessages } from "./fr/EntityPageMessages";
import { errorBoundaryMessages } from "./fr/ErrorBoundaryMessages";
import { layoutMessages } from "./fr/LayoutMessages";
import { loginPageMessages } from "./fr/LoginPageMessages";
import { modelPageMessages } from "./fr/ModelPageMessages";
import { preferencesPageMessages } from "./fr/PreferencesPageMessages";
import { sessionMessages } from "./fr/SessionMessages";

export const messages = {
  ...sessionMessages,
  ...dashboardPageMessages,
  ...preferencesPageMessages,
  ...errorBoundaryMessages,
  ...layoutMessages,
  ...modelPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
} satisfies Messages;
