import { type Messages } from "./Messages";
import { dashboardPageMessages } from "./en/DashboardPageMessages";
import { entityPageMessages } from "./en/EntityPageMessages";
import { errorBoundaryMessages } from "./en/ErrorBoundaryMessages";
import { layoutMessages } from "./en/LayoutMessages";
import { loginPageMessages } from "./en/LoginPageMessages";
import { modelPageMessages } from "./en/ModelPageMessages";
import { modelsPageMessages } from "./en/ModelsPageMessages";
import { preferencesPageMessages } from "./en/PreferencesPageMessages";
import { sessionMessages } from "./en/SessionMessages";

export const messages = {
  ...sessionMessages,
  ...dashboardPageMessages,
  ...preferencesPageMessages,
  ...errorBoundaryMessages,
  ...layoutMessages,
  ...modelsPageMessages,
  ...modelPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
} satisfies Messages;
